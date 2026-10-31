package com.personal.marketnote.user.adapter.out.vendor.authentication;

import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.common.domain.exception.token.UnsupportedCodeException;
import com.personal.marketnote.common.utility.http.cookie.HttpCookieName;
import com.personal.marketnote.common.utility.http.cookie.HttpCookieObject;
import com.personal.marketnote.common.utility.http.cookie.HttpCookieUtils;
import com.personal.marketnote.user.adapter.in.web.authentication.request.OAuth2LoginRequest;
import com.personal.marketnote.user.adapter.in.web.authentication.response.WebBasedTokenRefreshResponse;
import com.personal.marketnote.user.port.in.usecase.authentication.Oauth2LoginUseCase;
import com.personal.marketnote.user.port.out.authentication.SaveRefreshTokenPort;
import com.personal.marketnote.user.security.token.dto.GrantedTokenInfo;
import com.personal.marketnote.user.security.token.support.TokenSupport;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.utility.jwt.JwtUtil;
import com.personal.marketnote.user.utility.jwt.claims.TokenClaims;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebBasedAuthenticationServiceAdapterTest {

    private WebBasedAuthenticationServiceAdapter adapter;

    @Mock
    private Oauth2LoginUseCase oauth2LoginUseCase;

    @Mock
    private TokenSupport tokenSupport;

    @Mock
    private HttpCookieUtils httpCookieUtils;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SaveRefreshTokenPort saveRefreshTokenPort;

    @BeforeEach
    void setUp() {
        adapter = new WebBasedAuthenticationServiceAdapter(
                oauth2LoginUseCase,
                tokenSupport,
                "https://api.example.com",
                List.of("https://www.example.com"),
                httpCookieUtils,
                jwtUtil,
                saveRefreshTokenPort
        );
        ReflectionTestUtils.setField(adapter, "refreshTokenTtlMillis", 1209600000L);
    }

    @Nested
    @DisplayName("issueNewAccessToken - 액세스 토큰 재발급")
    class IssueNewAccessTokenTest {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 새 액세스 토큰을 발급한다")
        void shouldIssueNewAccessTokenWithValidRefreshToken() {
            // given
            String refreshToken = "valid-refresh-token";
            GrantedTokenInfo grantedTokenInfo = new GrantedTokenInfo(
                    "new-access-token", "new-refresh-token", "1", AuthVendor.NATIVE, null, null, null
            );
            when(tokenSupport.refreshToken(refreshToken)).thenReturn(grantedTokenInfo);

            TokenClaims claims = mock(TokenClaims.class);
            when(claims.getExpirationAt()).thenReturn(LocalDateTime.now().plusDays(10));
            when(jwtUtil.parseRefreshToken(refreshToken)).thenReturn(claims);

            HttpCookieObject cookieObject = mock(HttpCookieObject.class);
            when(cookieObject.asSetCookieHeaderValue()).thenReturn("refresh_token=valid-refresh-token; HttpOnly; Secure");
            when(httpCookieUtils.generateHttpOnlyCookie(eq(HttpCookieName.REFRESH_TOKEN), anyString(), anyLong()))
                    .thenReturn(cookieObject);

            // when
            WebBasedTokenRefreshResponse response = adapter.issueNewAccessToken(refreshToken);

            // then
            assertThat(response.accessToken().getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.headers().getFirst("Set-Cookie")).contains("refresh_token");
        }

        @Test
        @DisplayName("리프레시 토큰 TTL이 1일 미만이면 SaveRefreshTokenPort로 새 토큰을 저장한다")
        void shouldSaveNewRefreshTokenViaSaveRefreshTokenPortWhenTtlLessThanOneDay() {
            // given
            String refreshToken = "expiring-refresh-token";
            GrantedTokenInfo grantedTokenInfo = new GrantedTokenInfo(
                    "new-access-token", "new-refresh-token", "5", AuthVendor.NATIVE, null, null, null
            );
            when(tokenSupport.refreshToken(refreshToken)).thenReturn(grantedTokenInfo);

            TokenClaims claims = mock(TokenClaims.class);
            when(claims.getExpirationAt()).thenReturn(LocalDateTime.now().plusHours(12));
            when(jwtUtil.parseRefreshToken(refreshToken)).thenReturn(claims);

            HttpCookieObject cookieObject = mock(HttpCookieObject.class);
            when(cookieObject.asSetCookieHeaderValue()).thenReturn("refresh_token=new-refresh-token; HttpOnly; Secure");
            when(httpCookieUtils.generateHttpOnlyCookie(eq(HttpCookieName.REFRESH_TOKEN), anyString(), anyLong()))
                    .thenReturn(cookieObject);

            // when
            adapter.issueNewAccessToken(refreshToken);

            // then
            verify(saveRefreshTokenPort).saveByUserId(5L, "new-refresh-token", 1209600000L);
        }

        @Test
        @DisplayName("리프레시 토큰 TTL이 1일 이상이면 저장하지 않는다")
        void shouldNotSaveWhenTtlMoreThanOneDay() {
            // given
            String refreshToken = "fresh-refresh-token";
            GrantedTokenInfo grantedTokenInfo = new GrantedTokenInfo(
                    "new-access-token", "new-refresh-token", "1", AuthVendor.NATIVE, null, null, null
            );
            when(tokenSupport.refreshToken(refreshToken)).thenReturn(grantedTokenInfo);

            TokenClaims claims = mock(TokenClaims.class);
            when(claims.getExpirationAt()).thenReturn(LocalDateTime.now().plusDays(10));
            when(jwtUtil.parseRefreshToken(refreshToken)).thenReturn(claims);

            HttpCookieObject cookieObject = mock(HttpCookieObject.class);
            when(cookieObject.asSetCookieHeaderValue()).thenReturn("refresh_token=fresh-refresh-token; HttpOnly; Secure");
            when(httpCookieUtils.generateHttpOnlyCookie(eq(HttpCookieName.REFRESH_TOKEN), anyString(), anyLong()))
                    .thenReturn(cookieObject);

            // when
            adapter.issueNewAccessToken(refreshToken);

            // then
            verifyNoInteractions(saveRefreshTokenPort);
        }

        @Test
        @DisplayName("리프레시 토큰이 null이면 InvalidRefreshTokenException을 던진다")
        void shouldThrowExceptionWhenRefreshTokenIsNull() {
            // when & then
            assertThatThrownBy(() -> adapter.issueNewAccessToken(null))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        @DisplayName("리프레시 토큰이 빈 문자열이면 InvalidRefreshTokenException을 던진다")
        void shouldThrowExceptionWhenRefreshTokenIsEmpty() {
            // when & then
            assertThatThrownBy(() -> adapter.issueNewAccessToken(""))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }
    }

    @Nested
    @DisplayName("HTTP 4xx/5xx 응답 예외 전파")
    class HttpErrorPropagationTest {

        @Test
        @DisplayName("리프레시 토큰 갱신 시 하위 토큰 공급자의 HTTP 4xx 응답이 InvalidRefreshTokenException으로 변환되어 전파된다")
        void shouldPropagateInvalidRefreshTokenWhenVendorRespondsWith4xx() {
            // given
            String refreshToken = "invalid-refresh-token";
            InvalidRefreshTokenException vendor4xx = new InvalidRefreshTokenException(
                    "vendor responded with 401",
                    HttpClientErrorException.create(
                            HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null
                    )
            );
            when(tokenSupport.refreshToken(refreshToken)).thenThrow(vendor4xx);

            // when & then
            assertThatThrownBy(() -> adapter.issueNewAccessToken(refreshToken))
                    .isInstanceOf(InvalidRefreshTokenException.class);
            verify(tokenSupport).refreshToken(refreshToken);
            verifyNoInteractions(saveRefreshTokenPort);
        }

        @Test
        @DisplayName("리프레시 토큰 갱신 시 하위 토큰 공급자의 HTTP 5xx 응답이 런타임 예외로 전파된다")
        void shouldPropagateServerErrorWhenVendorRespondsWith5xx() {
            // given
            String refreshToken = "any-refresh-token";
            HttpServerErrorException vendor5xx = HttpServerErrorException.create(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null, null, null
            );
            when(tokenSupport.refreshToken(refreshToken)).thenThrow(vendor5xx);

            // when & then
            assertThatThrownBy(() -> adapter.issueNewAccessToken(refreshToken))
                    .isInstanceOf(HttpServerErrorException.class);
            verify(tokenSupport).refreshToken(refreshToken);
            verifyNoInteractions(saveRefreshTokenPort);
        }

        @Test
        @DisplayName("OAuth2 로그인 시 하위 UseCase의 UnsupportedCodeException이 그대로 전파된다")
        void shouldPropagateUnsupportedCodeExceptionFromLoginUseCase() {
            // given
            HttpServletRequest servletRequest = mock(HttpServletRequest.class);
            when(servletRequest.getRequestURI()).thenReturn("/api/v1/oauth2/login");

            OAuth2LoginRequest loginRequest = new OAuth2LoginRequest(
                    "KAKAO", "bad-code", null, servletRequest
            );

            when(oauth2LoginUseCase.loginByOAuth2("bad-code", "https://api.example.com/api/v1/oauth2/login", AuthVendor.KAKAO))
                    .thenThrow(new UnsupportedCodeException("invalid auth code"));

            // when & then
            assertThatThrownBy(() -> adapter.loginByOAuth2(loginRequest))
                    .isInstanceOf(UnsupportedCodeException.class);
        }

        @Test
        @DisplayName("OAuth2 로그인 시 하위 UseCase의 InvalidRefreshTokenException이 그대로 전파된다")
        void shouldPropagateInvalidRefreshTokenExceptionFromLoginUseCase() {
            // given
            HttpServletRequest servletRequest = mock(HttpServletRequest.class);
            when(servletRequest.getRequestURI()).thenReturn("/api/v1/oauth2/login");

            OAuth2LoginRequest loginRequest = new OAuth2LoginRequest(
                    "GOOGLE", "auth-code", null, servletRequest
            );

            InvalidRefreshTokenException vendor4xx = new InvalidRefreshTokenException(
                    "google responded with 400",
                    HttpClientErrorException.create(
                            HttpStatus.BAD_REQUEST, "Bad Request", null, null, null
                    )
            );
            when(oauth2LoginUseCase.loginByOAuth2("auth-code", "https://api.example.com/api/v1/oauth2/login", AuthVendor.GOOGLE))
                    .thenThrow(vendor4xx);

            // when & then
            assertThatThrownBy(() -> adapter.loginByOAuth2(loginRequest))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }
    }
}
