package com.personal.marketnote.common.utility.http.cookie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpCookieUtilsTest {

    @Mock
    private HttpCookieObjectFactory httpCookieObjectFactory;

    @InjectMocks
    private HttpCookieUtils httpCookieUtils;

    @Test
    @DisplayName("HttpOnly 쿠키 생성 시 httpOnly가 true로 전달된다")
    void shouldCreateHttpOnlyCookieWithHttpOnlyTrue() {
        // given
        HttpCookieObject expected = () -> "cookie-value";
        when(httpCookieObjectFactory.create(HttpCookieName.ACCESS_TOKEN, null, "token-value", 3600L, true))
                .thenReturn(expected);

        // when
        HttpCookieObject result = httpCookieUtils.generateHttpOnlyCookie(HttpCookieName.ACCESS_TOKEN, "token-value", 3600L);

        // then
        assertThat(result).isEqualTo(expected);
        verify(httpCookieObjectFactory).create(HttpCookieName.ACCESS_TOKEN, null, "token-value", 3600L, true);
    }

    @Test
    @DisplayName("JS 접근 가능 쿠키 생성 시 httpOnly가 false로 전달된다")
    void shouldCreateJsAccessibleCookieWithHttpOnlyFalse() {
        // given
        HttpCookieObject expected = () -> "cookie-value";
        when(httpCookieObjectFactory.create(HttpCookieName.USER_NAME, null, "홍길동", 7200L, false))
                .thenReturn(expected);

        // when
        HttpCookieObject result = httpCookieUtils.generateJsAccessibleCookie(HttpCookieName.USER_NAME, "홍길동", 7200L);

        // then
        assertThat(result).isEqualTo(expected);
        verify(httpCookieObjectFactory).create(HttpCookieName.USER_NAME, null, "홍길동", 7200L, false);
    }

    @Test
    @DisplayName("쿠키 무효화 시 빈 값과 cookieAge 0으로 생성된다")
    void shouldInvalidateCookieWithEmptyValueAndZeroAge() {
        // given
        HttpCookieObject expected = () -> "invalidated";
        when(httpCookieObjectFactory.create(HttpCookieName.REFRESH_TOKEN, null, "", 0L, true))
                .thenReturn(expected);

        // when
        HttpCookieObject result = httpCookieUtils.invalidateCookie(HttpCookieName.REFRESH_TOKEN, true);

        // then
        assertThat(result).isEqualTo(expected);
        verify(httpCookieObjectFactory).create(HttpCookieName.REFRESH_TOKEN, null, "", 0L, true);
    }

    @Test
    @DisplayName("쿠키 무효화 시 httpOnly false로도 생성할 수 있다")
    void shouldInvalidateCookieWithHttpOnlyFalse() {
        // given
        HttpCookieObject expected = () -> "invalidated";
        when(httpCookieObjectFactory.create(HttpCookieName.USER_ID, null, "", 0L, false))
                .thenReturn(expected);

        // when
        HttpCookieObject result = httpCookieUtils.invalidateCookie(HttpCookieName.USER_ID, false);

        // then
        assertThat(result).isEqualTo(expected);
        verify(httpCookieObjectFactory).create(HttpCookieName.USER_ID, null, "", 0L, false);
    }
}
