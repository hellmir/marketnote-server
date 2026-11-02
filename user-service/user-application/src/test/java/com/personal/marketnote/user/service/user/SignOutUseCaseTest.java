package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.utility.http.cookie.HttpCookieName;
import com.personal.marketnote.common.utility.http.cookie.HttpCookieObject;
import com.personal.marketnote.common.utility.http.cookie.HttpCookieUtils;
import com.personal.marketnote.user.port.out.authentication.DeleteRefreshTokenPort;
import com.personal.marketnote.user.port.out.authentication.ParseRefreshTokenPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignOutUseCaseTest {
    private static final String INVALID_USER_ID_COOKIE = "user_id=; Max-Age=0";
    private static final String INVALID_ACCESS_TOKEN_COOKIE = "access_token=; Max-Age=0";
    private static final String INVALID_REFRESH_TOKEN_COOKIE = "refresh_token=; Max-Age=0";

    @Mock
    private HttpCookieUtils httpCookieUtils;
    @Mock
    private DeleteRefreshTokenPort deleteRefreshTokenPort;
    @Mock
    private ParseRefreshTokenPort parseRefreshTokenPort;

    @InjectMocks
    private SignOutService signOutService;

    @Test
    @DisplayName("로그아웃 시 쿠키를 무효화한다")
    void signOut_success_invalidatesCookies() {
        // given
        String refreshToken = "refresh-token";
        Long userId = 11L;
        when(parseRefreshTokenPort.extractUserId(refreshToken)).thenReturn(userId);
        stubInvalidCookies();

        // when
        HttpHeaders result = signOutService.signOut(refreshToken);

        // then
        List<String> setCookies = result.get(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).containsExactly(
                INVALID_USER_ID_COOKIE,
                INVALID_ACCESS_TOKEN_COOKIE,
                INVALID_REFRESH_TOKEN_COOKIE
        );

        InOrder inOrder = inOrder(httpCookieUtils);
        inOrder.verify(httpCookieUtils).invalidateCookie(HttpCookieName.USER_ID, false);
        inOrder.verify(httpCookieUtils).invalidateCookie(HttpCookieName.ACCESS_TOKEN, false);
        inOrder.verify(httpCookieUtils).invalidateCookie(HttpCookieName.REFRESH_TOKEN, false);
        verifyNoMoreInteractions(httpCookieUtils);
    }

    @Test
    @DisplayName("로그아웃 시 리프레시 토큰의 화이트리스트를 삭제한다")
    void signOut_success_deletesRefreshTokenWhitelist() {
        // given
        String refreshToken = "refresh-token";
        Long userId = 10L;
        when(parseRefreshTokenPort.extractUserId(refreshToken)).thenReturn(userId);
        stubInvalidCookies();

        // when
        signOutService.signOut(refreshToken);

        // then
        InOrder inOrder = inOrder(parseRefreshTokenPort, deleteRefreshTokenPort);
        inOrder.verify(parseRefreshTokenPort).extractUserId(refreshToken);
        inOrder.verify(deleteRefreshTokenPort).deleteByUserId(userId);
        verifyNoMoreInteractions(parseRefreshTokenPort, deleteRefreshTokenPort);
    }

    @Test
    @DisplayName("로그아웃 중 리프레시 토큰 파싱에 실패하면 예외를 던진다")
    void signOut_parseRefreshTokenFails_throws() {
        // given
        String refreshToken = "invalid-token";
        IllegalArgumentException exception = new IllegalArgumentException("invalid");

        when(parseRefreshTokenPort.extractUserId(refreshToken)).thenThrow(exception);

        // expect
        assertThatThrownBy(() -> signOutService.signOut(refreshToken))
                .isSameAs(exception);

        verify(parseRefreshTokenPort).extractUserId(refreshToken);
        verifyNoInteractions(deleteRefreshTokenPort, httpCookieUtils);
    }

    @Test
    @DisplayName("로그아웃 중 리프레시 토큰 삭제에 실패하면 예외를 던진다")
    void signOut_deleteRefreshTokenFails_throws() {
        // given
        String refreshToken = "refresh-token";
        Long userId = 20L;
        IllegalStateException exception = new IllegalStateException("delete failed");

        when(parseRefreshTokenPort.extractUserId(refreshToken)).thenReturn(userId);
        doThrow(exception).when(deleteRefreshTokenPort).deleteByUserId(userId);

        // expect
        assertThatThrownBy(() -> signOutService.signOut(refreshToken))
                .isSameAs(exception);

        verify(parseRefreshTokenPort).extractUserId(refreshToken);
        verify(deleteRefreshTokenPort).deleteByUserId(userId);
        verifyNoInteractions(httpCookieUtils);
    }

    private void stubInvalidCookies() {
        HttpCookieObject userIdCookie = mock(HttpCookieObject.class);
        HttpCookieObject accessTokenCookie = mock(HttpCookieObject.class);
        HttpCookieObject refreshTokenCookie = mock(HttpCookieObject.class);

        when(httpCookieUtils.invalidateCookie(HttpCookieName.USER_ID, false)).thenReturn(userIdCookie);
        when(httpCookieUtils.invalidateCookie(HttpCookieName.ACCESS_TOKEN, false)).thenReturn(accessTokenCookie);
        when(httpCookieUtils.invalidateCookie(HttpCookieName.REFRESH_TOKEN, false)).thenReturn(refreshTokenCookie);
        when(userIdCookie.asSetCookieHeaderValue()).thenReturn(INVALID_USER_ID_COOKIE);
        when(accessTokenCookie.asSetCookieHeaderValue()).thenReturn(INVALID_ACCESS_TOKEN_COOKIE);
        when(refreshTokenCookie.asSetCookieHeaderValue()).thenReturn(INVALID_REFRESH_TOKEN_COOKIE);
    }
}
