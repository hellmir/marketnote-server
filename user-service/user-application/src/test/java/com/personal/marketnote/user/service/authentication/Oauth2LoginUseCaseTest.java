package com.personal.marketnote.user.service.authentication;

import com.personal.marketnote.common.domain.exception.illegalargument.novalue.OauthTokenNoValueException;
import com.personal.marketnote.common.domain.exception.token.UnsupportedCodeException;
import com.personal.marketnote.user.domain.user.Nickname;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.in.result.LoginResult;
import com.personal.marketnote.user.port.in.usecase.authentication.Oauth2LoginUseCase;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.security.token.dto.GrantedTokenInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2UserInfo;
import com.personal.marketnote.user.security.token.support.TokenSupport;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

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
        when(user.getNickname()).thenReturn(Nickname.of("tester"));

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

    @Nested
    @DisplayName("OAuth2 로그인 트랜잭션 경계 분리 검증")
    class TransactionBoundaryIsolation {

        @Test
        @DisplayName("Oauth2LoginService 클래스에는 @Transactional 어노테이션이 존재하지 않는다")
        void oauth2LoginService_hasNoClassLevelTransactional() {
            // expect
            Transactional classTransactional = Oauth2LoginService.class.getAnnotation(Transactional.class);
            assertThat(classTransactional)
                    .as("외부 OAuth2 서버 호출이 DB 트랜잭션 경계 안에서 수행되지 않도록 클래스 레벨 @Transactional 은 제거되어야 한다")
                    .isNull();
        }

        @Test
        @DisplayName("loginByOAuth2 메서드에는 @Transactional 어노테이션이 존재하지 않는다")
        void loginByOAuth2_hasNoMethodLevelTransactional() throws NoSuchMethodException {
            // given
            Method method = Oauth2LoginService.class.getMethod(
                    "loginByOAuth2", String.class, String.class, AuthVendor.class
            );

            // expect
            Transactional methodTransactional = method.getAnnotation(Transactional.class);
            assertThat(methodTransactional)
                    .as("DB 커넥션 점유 위험을 제거하기 위해 메서드 레벨 @Transactional 도 존재하지 않아야 한다")
                    .isNull();
        }

        @Test
        @DisplayName("Oauth2LoginUseCase 인터페이스의 loginByOAuth2 에도 @Transactional 이 부여되어 있지 않다")
        void oauth2LoginUseCase_interfaceHasNoTransactional() throws NoSuchMethodException {
            // given
            Transactional interfaceClassTransactional = Oauth2LoginUseCase.class.getAnnotation(Transactional.class);
            Method interfaceMethod = Oauth2LoginUseCase.class.getMethod(
                    "loginByOAuth2", String.class, String.class, AuthVendor.class
            );
            Transactional interfaceMethodTransactional = interfaceMethod.getAnnotation(Transactional.class);

            // expect
            assertThat(interfaceClassTransactional)
                    .as("인터페이스 레벨에도 @Transactional 이 없어야 Spring AOP 가 트랜잭션 경계를 적용하지 않는다")
                    .isNull();
            assertThat(interfaceMethodTransactional)
                    .as("인터페이스 메서드에도 @Transactional 이 없어야 Spring AOP 가 트랜잭션 경계를 적용하지 않는다")
                    .isNull();
        }

        @Test
        @DisplayName("외부 OAuth2 서버 호출 tokenSupport.grantToken 은 활성 트랜잭션 밖에서 실행된다")
        void grantToken_isInvokedOutsideActiveTransaction() {
            // given
            String code = "code";
            String redirectUri = "http://localhost/callback";
            AuthVendor authVendor = AuthVendor.KAKAO;
            String oidcId = "oidc-tx-1";
            GrantedTokenInfo tokenInfo = GrantedTokenInfo.builder()
                    .accessToken("access-token")
                    .refreshToken("refresh-token")
                    .id(oidcId)
                    .authVendor(authVendor)
                    .build();
            AtomicBoolean txActiveDuringGrantToken = new AtomicBoolean(true);

            when(tokenSupport.grantToken(code, redirectUri, authVendor)).thenAnswer(invocation -> {
                txActiveDuringGrantToken.set(TransactionSynchronizationManager.isActualTransactionActive());
                return tokenInfo;
            });
            when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(authVendor, oidcId)).thenReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> oauth2LoginService.loginByOAuth2(code, redirectUri, authVendor))
                    .isInstanceOf(OauthTokenNoValueException.class);

            // then
            assertThat(txActiveDuringGrantToken.get())
                    .as("외부 OAuth2 서버 호출 시점에는 DB 트랜잭션이 활성화되어 있지 않아야 한다 (커넥션 풀 고갈 방지)")
                    .isFalse();
        }

        @Test
        @DisplayName("DB 조회 findAllStatusUserByAuthVendorAndOidcId 는 서비스 레벨 트랜잭션 없이 호출된다")
        void findAllStatusUser_isInvokedWithoutServiceLevelTransaction() {
            // given
            String code = "code";
            String redirectUri = "http://localhost/callback";
            AuthVendor authVendor = AuthVendor.GOOGLE;
            String oidcId = "oidc-tx-2";
            OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                    .id(oidcId)
                    .name("tx-user")
                    .build();
            GrantedTokenInfo tokenInfo = GrantedTokenInfo.builder()
                    .accessToken("access-token")
                    .refreshToken("refresh-token")
                    .id(oidcId)
                    .authVendor(authVendor)
                    .userInfo(userInfo)
                    .build();
            AtomicBoolean txActiveDuringFindUser = new AtomicBoolean(true);

            when(tokenSupport.grantToken(code, redirectUri, authVendor)).thenReturn(tokenInfo);
            when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(authVendor, oidcId)).thenAnswer(invocation -> {
                txActiveDuringFindUser.set(TransactionSynchronizationManager.isActualTransactionActive());
                return Optional.empty();
            });

            // when
            LoginResult result = oauth2LoginService.loginByOAuth2(code, redirectUri, authVendor);

            // then
            assertThat(result.isNewUser()).isTrue();
            assertThat(txActiveDuringFindUser.get())
                    .as("서비스 레벨 @Transactional 이 제거되었으므로, 어댑터 진입 이전에는 활성 트랜잭션이 없어야 한다")
                    .isFalse();
        }
    }
}
