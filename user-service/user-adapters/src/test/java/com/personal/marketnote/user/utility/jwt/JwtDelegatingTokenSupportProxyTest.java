package com.personal.marketnote.user.utility.jwt;

import com.personal.marketnote.common.domain.exception.token.InvalidAccessTokenException;
import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.UserNotActiveException;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.security.token.dto.GrantedTokenInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2AuthenticationInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2UserInfo;
import com.personal.marketnote.user.security.token.support.TokenProcessor;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.utility.jwt.claims.TokenClaims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtDelegatingTokenSupportProxyTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FindUserPort findUserPort;

    @Mock
    private TokenProcessor kakaoProcessor;

    @Mock
    private TokenProcessor googleProcessor;

    private JwtDelegatingTokenSupportProxy createProxy(TokenProcessor... processors) {
        for (TokenProcessor processor : processors) {
            if (processor == kakaoProcessor) {
                when(processor.getAuthVendor()).thenReturn(AuthVendor.KAKAO);
                continue;
            }
            if (processor == googleProcessor) {
                when(processor.getAuthVendor()).thenReturn(AuthVendor.GOOGLE);
            }
        }
        return new JwtDelegatingTokenSupportProxy(List.of(processors), jwtUtil, findUserPort);
    }

    @Nested
    @DisplayName("grantToken - OAuth2 코드 기반 JWT 발급")
    class GrantTokenTest {

        @Test
        @DisplayName("기존 활성 회원의 OAuth2 코드로 자체 JWT 액세스/리프레시 토큰을 발급한다")
        void shouldIssueJwtTokensWhenExistingActiveUser() {
            // given
            JwtDelegatingTokenSupportProxy proxy = createProxy(kakaoProcessor);

            GrantedTokenInfo thirdPartyToken = GrantedTokenInfo.builder()
                    .accessToken("third-party-access")
                    .refreshToken("third-party-refresh")
                    .id("kakao-oidc-1")
                    .authVendor(AuthVendor.KAKAO)
                    .build();
            when(kakaoProcessor.grantToken("auth-code", "https://example.com/callback"))
                    .thenReturn(thirdPartyToken);

            User user = mock(User.class);
            when(user.isGuest()).thenReturn(false);
            when(user.isActive()).thenReturn(true);
            when(user.getId()).thenReturn(100L);
            when(user.getRole()).thenReturn(Role.of("ROLE_BUYER", "구매자"));
            when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(AuthVendor.KAKAO, "kakao-oidc-1"))
                    .thenReturn(Optional.of(user));

            when(jwtUtil.generateAccessToken("kakao-oidc-1", 100L, List.of("ROLE_BUYER"), AuthVendor.KAKAO))
                    .thenReturn("jwt-access-token");
            when(jwtUtil.generateRefreshToken("kakao-oidc-1", List.of("ROLE_BUYER"), AuthVendor.KAKAO))
                    .thenReturn("jwt-refresh-token");

            // when
            GrantedTokenInfo result = proxy.grantToken("auth-code", "https://example.com/callback", AuthVendor.KAKAO);

            // then
            assertThat(result.accessToken()).isEqualTo("jwt-access-token");
            assertThat(result.refreshToken()).isEqualTo("jwt-refresh-token");
            assertThat(result.id()).isEqualTo("kakao-oidc-1");
            assertThat(result.authVendor()).isEqualTo(AuthVendor.KAKAO);
            assertThat(result.userInfo()).isNull();
            verify(kakaoProcessor).grantToken("auth-code", "https://example.com/callback");
            verify(kakaoProcessor, never()).retrieveUserInfo("third-party-access");
        }

        @Test
        @DisplayName("신규 게스트 회원의 경우 3rd party에서 사용자 정보를 조회하여 포함한다")
        void shouldRetrieveUserInfoWhenGuestUser() {
            // given
            JwtDelegatingTokenSupportProxy proxy = createProxy(kakaoProcessor);

            GrantedTokenInfo thirdPartyToken = GrantedTokenInfo.builder()
                    .accessToken("third-party-access")
                    .refreshToken("third-party-refresh")
                    .id("kakao-oidc-new")
                    .authVendor(AuthVendor.KAKAO)
                    .build();
            when(kakaoProcessor.grantToken("auth-code", "https://example.com/callback"))
                    .thenReturn(thirdPartyToken);

            when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(AuthVendor.KAKAO, "kakao-oidc-new"))
                    .thenReturn(Optional.empty());

            OAuth2UserInfo userInfoFromVendor = OAuth2UserInfo.builder()
                    .id("kakao-oidc-new")
                    .name("홍길동")
                    .build();
            when(kakaoProcessor.retrieveUserInfo("third-party-access")).thenReturn(userInfoFromVendor);

            when(jwtUtil.generateAccessToken("kakao-oidc-new", null, List.of("ROLE_GUEST"), AuthVendor.KAKAO))
                    .thenReturn("jwt-access-token");
            when(jwtUtil.generateRefreshToken("kakao-oidc-new", List.of("ROLE_GUEST"), AuthVendor.KAKAO))
                    .thenReturn("jwt-refresh-token");

            // when
            GrantedTokenInfo result = proxy.grantToken("auth-code", "https://example.com/callback", AuthVendor.KAKAO);

            // then
            assertThat(result.accessToken()).isEqualTo("jwt-access-token");
            assertThat(result.userInfo()).isSameAs(userInfoFromVendor);
            verify(kakaoProcessor).retrieveUserInfo("third-party-access");
        }

        @Test
        @DisplayName("기존 비활성 회원의 경우 UserNotActiveException을 던진다")
        void shouldThrowUserNotActiveExceptionWhenInactiveUser() {
            // given
            JwtDelegatingTokenSupportProxy proxy = createProxy(kakaoProcessor);

            GrantedTokenInfo thirdPartyToken = GrantedTokenInfo.builder()
                    .accessToken("third-party-access")
                    .refreshToken("third-party-refresh")
                    .id("kakao-oidc-inactive")
                    .authVendor(AuthVendor.KAKAO)
                    .build();
            when(kakaoProcessor.grantToken("auth-code", "https://example.com/callback"))
                    .thenReturn(thirdPartyToken);

            User user = mock(User.class);
            when(user.isGuest()).thenReturn(false);
            when(user.isActive()).thenReturn(false);
            when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(AuthVendor.KAKAO, "kakao-oidc-inactive"))
                    .thenReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> proxy.grantToken(
                    "auth-code", "https://example.com/callback", AuthVendor.KAKAO
            )).isInstanceOf(UserNotActiveException.class);
        }
    }

    @Nested
    @DisplayName("authenticate - 액세스 토큰 검증")
    class AuthenticateTest {

        @Test
        @DisplayName("자체 JWT가 유효하면 JwtUtil로 파싱한 인증 정보를 반환한다")
        void shouldReturnAuthenticationInfoFromJwt() {
            // given
            JwtDelegatingTokenSupportProxy proxy = createProxy(kakaoProcessor);

            TokenClaims claims = TokenClaims.builder()
                    .id("kakao-oidc-1")
                    .userId(100L)
                    .authVendor(AuthVendor.KAKAO)
                    .build();
            when(jwtUtil.parseAccessToken("jwt-access-token")).thenReturn(claims);

            // when
            OAuth2AuthenticationInfo result = proxy.authenticate("jwt-access-token");

            // then
            assertThat(result.id()).isEqualTo("kakao-oidc-1");
            assertThat(result.userId()).isEqualTo(100L);
            assertThat(result.authVendor()).isEqualTo(AuthVendor.KAKAO);
            verify(kakaoProcessor, never()).authenticate("jwt-access-token");
        }

        @Test
        @DisplayName("자체 JWT가 만료되었으면 공급자 순회로 폴백하여 인증을 시도한다")
        void shouldFallbackWhenJwtExpired() {
            // given
            JwtDelegatingTokenSupportProxy proxy = createProxy(kakaoProcessor);

            when(jwtUtil.parseAccessToken("expired-jwt"))
                    .thenThrow(new ExpiredJwtException(null, null, "expired"));

            OAuth2AuthenticationInfo vendorResult = OAuth2AuthenticationInfo.builder()
                    .id("kakao-oidc-2")
                    .userId(null)
                    .authVendor(AuthVendor.KAKAO)
                    .build();
            when(kakaoProcessor.authenticate("expired-jwt")).thenReturn(vendorResult);

            // when
            OAuth2AuthenticationInfo result = proxy.authenticate("expired-jwt");

            // then
            assertThat(result).isSameAs(vendorResult);
            verify(kakaoProcessor).authenticate("expired-jwt");
        }

        @Test
        @DisplayName("자체 JWT 서명이 불일치하면 공급자 순회로 폴백하고 모두 실패 시 InvalidAccessTokenException을 던진다")
        void shouldThrowInvalidAccessTokenWhenSignatureMismatchAndVendorsFail() {
            // given
            JwtDelegatingTokenSupportProxy proxy = createProxy(kakaoProcessor, googleProcessor);

            when(jwtUtil.parseAccessToken("tampered-jwt"))
                    .thenThrow(new SignatureException("signature mismatch"));
            when(kakaoProcessor.authenticate("tampered-jwt"))
                    .thenThrow(new InvalidAccessTokenException("not a kakao token"));
            when(googleProcessor.authenticate("tampered-jwt"))
                    .thenThrow(new InvalidAccessTokenException("not a google token"));

            // when & then
            assertThatThrownBy(() -> proxy.authenticate("tampered-jwt"))
                    .isInstanceOf(InvalidAccessTokenException.class);
            verify(kakaoProcessor).authenticate("tampered-jwt");
            verify(googleProcessor).authenticate("tampered-jwt");
        }

        @Test
        @DisplayName("자체 JWT 형식 오류(MalformedJwtException)도 폴백 경로로 이어진다")
        void shouldFallbackWhenJwtMalformed() {
            // given
            JwtDelegatingTokenSupportProxy proxy = createProxy(kakaoProcessor);

            when(jwtUtil.parseAccessToken("malformed-jwt"))
                    .thenThrow(new MalformedJwtException("malformed"));

            OAuth2AuthenticationInfo vendorResult = OAuth2AuthenticationInfo.builder()
                    .id("kakao-oidc-3")
                    .authVendor(AuthVendor.KAKAO)
                    .build();
            when(kakaoProcessor.authenticate("malformed-jwt")).thenReturn(vendorResult);

            // when
            OAuth2AuthenticationInfo result = proxy.authenticate("malformed-jwt");

            // then
            assertThat(result).isSameAs(vendorResult);
        }
    }

    @Nested
    @DisplayName("refreshToken - 리프레시 토큰 검증 및 재발급")
    class RefreshTokenTest {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 새 액세스/리프레시 토큰을 발급한다")
        void shouldReissueTokensWhenRefreshTokenValid() {
            // given
            JwtDelegatingTokenSupportProxy proxy = createProxy(kakaoProcessor);

            TokenClaims claims = TokenClaims.builder()
                    .id("200")
                    .userId(200L)
                    .roleIds(List.of("ROLE_BUYER"))
                    .authVendor(AuthVendor.KAKAO)
                    .build();
            when(jwtUtil.parseRefreshToken("valid-refresh")).thenReturn(claims);
            when(jwtUtil.generateAccessToken("200", 200L, List.of("ROLE_BUYER"), AuthVendor.KAKAO))
                    .thenReturn("new-access");
            when(jwtUtil.generateRefreshToken("200", List.of("ROLE_BUYER"), AuthVendor.KAKAO))
                    .thenReturn("new-refresh");

            // when
            GrantedTokenInfo result = proxy.refreshToken("valid-refresh");

            // then
            assertThat(result.accessToken()).isEqualTo("new-access");
            assertThat(result.refreshToken()).isEqualTo("new-refresh");
            assertThat(result.id()).isEqualTo("200");
            assertThat(result.authVendor()).isEqualTo(AuthVendor.KAKAO);
        }

        @Test
        @DisplayName("리프레시 토큰이 만료되면 InvalidRefreshTokenException을 던진다")
        void shouldThrowInvalidRefreshTokenWhenExpired() {
            // given
            JwtDelegatingTokenSupportProxy proxy = createProxy(kakaoProcessor);

            when(jwtUtil.parseRefreshToken("expired-refresh"))
                    .thenThrow(new ExpiredJwtException(null, null, "expired"));

            // when & then
            assertThatThrownBy(() -> proxy.refreshToken("expired-refresh"))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        @DisplayName("리프레시 토큰 서명이 불일치하면 InvalidRefreshTokenException을 던진다")
        void shouldThrowInvalidRefreshTokenWhenSignatureMismatch() {
            // given
            JwtDelegatingTokenSupportProxy proxy = createProxy(kakaoProcessor);

            when(jwtUtil.parseRefreshToken("tampered-refresh"))
                    .thenThrow(new SignatureException("signature mismatch"));

            // when & then
            assertThatThrownBy(() -> proxy.refreshToken("tampered-refresh"))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }
    }
}
