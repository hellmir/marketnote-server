package com.personal.marketnote.user.adapter.out.vendor.authentication;

import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.common.utility.http.cookie.HttpCookieName;
import com.personal.marketnote.common.utility.http.cookie.HttpCookieObject;
import com.personal.marketnote.common.utility.http.cookie.HttpCookieUtils;
import com.personal.marketnote.user.adapter.in.web.authentication.response.WebBasedTokenRefreshResponse;
import com.personal.marketnote.user.port.in.usecase.authentication.Oauth2LoginUseCase;
import com.personal.marketnote.user.security.token.dto.GrantedTokenInfo;
import com.personal.marketnote.user.security.token.support.TokenSupport;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.utility.jwt.JwtUtil;
import com.personal.marketnote.user.utility.jwt.claims.TokenClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        adapter = new WebBasedAuthenticationServiceAdapter(
                oauth2LoginUseCase,
                tokenSupport,
                "https://api.example.com",
                List.of("https://www.example.com"),
                httpCookieUtils,
                jwtUtil,
                stringRedisTemplate
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
        @DisplayName("리프레시 토큰 TTL이 1일 미만이면 Redis에 새 토큰을 저장한다")
        void shouldStoreNewRefreshTokenInRedisWhenTtlLessThanOneDay() {
            // given
            String refreshToken = "expiring-refresh-token";
            GrantedTokenInfo grantedTokenInfo = new GrantedTokenInfo(
                    "new-access-token", "new-refresh-token", "5", AuthVendor.NATIVE, null, null, null
            );
            when(tokenSupport.refreshToken(refreshToken)).thenReturn(grantedTokenInfo);

            TokenClaims claims = mock(TokenClaims.class);
            when(claims.getExpirationAt()).thenReturn(LocalDateTime.now().plusHours(12));
            when(jwtUtil.parseRefreshToken(refreshToken)).thenReturn(claims);

            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

            HttpCookieObject cookieObject = mock(HttpCookieObject.class);
            when(cookieObject.asSetCookieHeaderValue()).thenReturn("refresh_token=new-refresh-token; HttpOnly; Secure");
            when(httpCookieUtils.generateHttpOnlyCookie(eq(HttpCookieName.REFRESH_TOKEN), anyString(), anyLong()))
                    .thenReturn(cookieObject);

            // when
            adapter.issueNewAccessToken(refreshToken);

            // then
            verify(valueOperations).set(
                    eq("refreshToken:5"),
                    argThat(value -> value.startsWith("r")),
                    eq(1209600000L),
                    eq(TimeUnit.MILLISECONDS)
            );
        }

        @Test
        @DisplayName("리프레시 토큰 TTL이 1일 이상이면 Redis에 저장하지 않는다")
        void shouldNotStoreInRedisWhenTtlMoreThanOneDay() {
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
            verifyNoInteractions(stringRedisTemplate);
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
}
