package com.personal.marketnote.user.service.authentication;

import com.personal.marketnote.common.domain.exception.illegalargument.novalue.OauthTokenNoValueException;
import com.personal.marketnote.common.domain.exception.token.UnsupportedCodeException;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.in.result.LoginResult;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.security.token.dto.GrantedTokenInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2UserInfo;
import com.personal.marketnote.user.security.token.support.TokenSupport;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Oauth2LoginUseCaseTest {
    @Mock
    private TokenSupport tokenSupport;

    @Mock
    private FindUserPort findUserPort;

    @InjectMocks
    private Oauth2LoginService oauth2LoginService;

    @Test
    @DisplayName("가입된 소셜 회원이 로그인을 시도하는 경우 기존 회원 닉네임 기반의 로그인 결과를 반환한다")
    void loginByOAuth2_existingUser_returnsLoginResult() throws Exception {
        // given
        String code = "code";
        String redirectUri = "http://localhost/callback";
        AuthVendor authVendor = AuthVendor.KAKAO;
        String oidcId = "oidc-123";
        GrantedTokenInfo tokenInfo = GrantedTokenInfo.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .id(oidcId)
                .authVendor(authVendor)
                .build();
        User user = mock(User.class);

        when(tokenSupport.grantToken(code, redirectUri, authVendor)).thenReturn(tokenInfo);
        when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(authVendor, oidcId)).thenReturn(Optional.of(user));
        when(user.isActive()).thenReturn(true);
        when(user.getNickname()).thenReturn("tester");

        // when
        LoginResult result = oauth2LoginService.loginByOAuth2(code, redirectUri, authVendor);

        // then
        assertThat(result.isNewUser()).isFalse();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getNickname()).isEqualTo("tester");

        verify(tokenSupport).grantToken(code, redirectUri, authVendor);
        verify(findUserPort).findAllStatusUserByAuthVendorAndOidcId(authVendor, oidcId);
        verify(user).getNickname();
        verifyNoMoreInteractions(tokenSupport, findUserPort, user);
    }

    @Test
    @DisplayName("신규 소셜 회원이 로그인을 시도하는 경우 사용자 정보 기반으로 로그인 결과를 반환한다")
    void loginByOAuth2_newUser_returnsLoginResult() {
        // given
        String code = "code";
        String redirectUri = "http://localhost/callback";
        AuthVendor authVendor = AuthVendor.GOOGLE;
        String oidcId = "oidc-999";
        OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                .id(oidcId)
                .name("new-user")
                .build();
        GrantedTokenInfo tokenInfo = GrantedTokenInfo.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .id(oidcId)
                .authVendor(authVendor)
                .userInfo(userInfo)
                .build();

        when(tokenSupport.grantToken(code, redirectUri, authVendor)).thenReturn(tokenInfo);
        when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(authVendor, oidcId)).thenReturn(Optional.empty());

        // when
        LoginResult result = oauth2LoginService.loginByOAuth2(code, redirectUri, authVendor);

        // then
        assertThat(result.isNewUser()).isTrue();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getNickname()).isEqualTo("new-user");

        verify(tokenSupport).grantToken(code, redirectUri, authVendor);
        verify(findUserPort).findAllStatusUserByAuthVendorAndOidcId(authVendor, oidcId);
        verifyNoMoreInteractions(tokenSupport, findUserPort);
    }

    @Test
    @DisplayName("신규 소셜 회원이 OAuth2 토큰 인증에 실패하는 경우 예외를 던진다")
    void loginByOAuth2_missingUserInfo_throws() {
        // given
        String code = "code";
        String redirectUri = "http://localhost/callback";
        AuthVendor authVendor = AuthVendor.APPLE;
        String oidcId = "oidc-404";
        GrantedTokenInfo tokenInfo = GrantedTokenInfo.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .id(oidcId)
                .authVendor(authVendor)
                .userInfo(null)
                .build();

        when(tokenSupport.grantToken(code, redirectUri, authVendor)).thenReturn(tokenInfo);
        when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(authVendor, oidcId)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> oauth2LoginService.loginByOAuth2(code, redirectUri, authVendor))
                .isInstanceOf(OauthTokenNoValueException.class);

        verify(tokenSupport).grantToken(code, redirectUri, authVendor);
        verify(findUserPort).findAllStatusUserByAuthVendorAndOidcId(authVendor, oidcId);
        verifyNoMoreInteractions(tokenSupport, findUserPort);
    }

    @Test
    @DisplayName("지원하지 않는 인증 코드로 소셜 로그인을 시도하면 예외를 던진다")
    void loginByOAuth2_unsupportedCode_throws() {
        // given
        String code = "bad-code";
        String redirectUri = "http://localhost/callback";
        AuthVendor authVendor = AuthVendor.KAKAO;
        UnsupportedCodeException exception = new UnsupportedCodeException("unsupported");

        when(tokenSupport.grantToken(code, redirectUri, authVendor)).thenThrow(exception);

        // expect
        assertThatThrownBy(() -> oauth2LoginService.loginByOAuth2(code, redirectUri, authVendor))
                .isSameAs(exception);

        verify(tokenSupport).grantToken(code, redirectUri, authVendor);
        verifyNoInteractions(findUserPort);
    }
}
