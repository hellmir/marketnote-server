package com.personal.marketnote.common.security.hmac;

import com.personal.marketnote.common.security.hmac.exception.HmacAuthenticationFailedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static com.personal.marketnote.common.security.hmac.HmacHeaderConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HmacAuthenticationFilterTest {
    private static final String SECRET_KEY = "test-hmac-secret-key";
    private static final long FIXED_TIME_MILLIS = 1710000000000L;
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.ofEpochMilli(FIXED_TIME_MILLIS), ZoneId.of("Asia/Seoul"));

    private HmacAuthenticationFilter hmacAuthenticationFilter;

    @Mock
    private HmacNonceValidator hmacNonceValidator;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        hmacAuthenticationFilter = new HmacAuthenticationFilter(SECRET_KEY, hmacNonceValidator, FIXED_CLOCK);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Bearer 토큰이 존재하면 필터를 스킵한다")
    void shouldSkipFilterWhenBearerTokenExists() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products");
        request.addHeader("Authorization", "Bearer some-jwt-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        hmacAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("HMAC 헤더가 모두 없으면 필터를 스킵한다")
    void shouldSkipFilterWhenNoHmacHeaders() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products");
        MockHttpServletResponse response = new MockHttpServletResponse();

        hmacAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("유효한 HMAC 헤더로 요청하면 인증에 성공하고 SecurityContext에 인증 정보가 설정된다")
    void shouldAuthenticateWithValidHmacHeaders() throws ServletException, IOException {
        String timestamp = String.valueOf(FIXED_TIME_MILLIS);
        String nonce = "550e8400-e29b-41d4-a716-446655440000";
        String signature = HmacSignatureGenerator.generate(SECRET_KEY, timestamp, nonce, "GET", "/api/v1/products");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products");
        request.addHeader(HEADER_SIGNATURE, signature);
        request.addHeader(HEADER_TIMESTAMP, timestamp);
        request.addHeader(HEADER_NONCE, nonce);
        MockHttpServletResponse response = new MockHttpServletResponse();

        hmacAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(hmacNonceValidator).validateAndStore(nonce);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("SERVICE");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_SERVICE");
    }

    @Test
    @DisplayName("HMAC 헤더가 일부만 존재하면 HmacAuthenticationFailedException이 발생한다")
    void shouldThrowWhenPartialHmacHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products");
        request.addHeader(HEADER_SIGNATURE, "some-signature");
        request.addHeader(HEADER_TIMESTAMP, "1710000000000");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> hmacAuthenticationFilter.doFilter(request, response, filterChain))
                .isInstanceOf(HmacAuthenticationFailedException.class)
                .hasMessageContaining("필수 헤더가 누락");
    }

    @Test
    @DisplayName("Signature만 존재하면 HmacAuthenticationFailedException이 발생한다")
    void shouldThrowWhenOnlySignaturePresent() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products");
        request.addHeader(HEADER_SIGNATURE, "some-signature");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> hmacAuthenticationFilter.doFilter(request, response, filterChain))
                .isInstanceOf(HmacAuthenticationFailedException.class);
    }
}
