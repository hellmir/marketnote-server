package com.personal.marketnote.common.security.token.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonBearerTokenResolverTest {

    @Mock
    private HttpServletRequest request;

    private JsonBearerTokenResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new JsonBearerTokenResolver();
    }

    @Test
    @DisplayName("Authorization 헤더가 Bearer 스킴이면 토큰 부분만 잘라 반환한다")
    void shouldReturnTokenWhenAuthorizationStartsWithBearer() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer abc.def.ghi");

        String token = resolver.resolve(request);

        assertThat(token).isEqualTo("abc.def.ghi");
    }

    @Test
    @DisplayName("Authorization 헤더가 null이면 null을 반환한다")
    void shouldReturnNullWhenAuthorizationHeaderIsNull() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        String token = resolver.resolve(request);

        assertThat(token).isNull();
    }

    @Test
    @DisplayName("Authorization 헤더가 빈 문자열이면 null을 반환한다")
    void shouldReturnNullWhenAuthorizationHeaderIsBlank() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("");

        String token = resolver.resolve(request);

        assertThat(token).isNull();
    }

    @Test
    @DisplayName("Authorization 헤더가 Bearer 스킴으로 시작하지 않으면 null을 반환한다")
    void shouldReturnNullWhenSchemeIsNotBearer() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");

        String token = resolver.resolve(request);

        assertThat(token).isNull();
    }
}
