package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.http.cookie.HttpCookieName;
import com.personal.marketnote.common.utility.http.cookie.HttpCookieUtils;
import com.personal.marketnote.user.port.in.usecase.user.SignOutUseCase;
import com.personal.marketnote.user.port.out.authentication.DeleteRefreshTokenPort;
import com.personal.marketnote.user.port.out.authentication.ParseRefreshTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
@UseCase
public class SignOutService implements SignOutUseCase {
    private final HttpCookieUtils httpCookieUtils;
    private final DeleteRefreshTokenPort deleteRefreshTokenPort;
    private final ParseRefreshTokenPort parseRefreshTokenPort;

    @Override
    public HttpHeaders signOut(String refreshToken) {
        Long userId = parseRefreshTokenPort.extractUserId(refreshToken);
        deleteRefreshTokenPort.deleteByUserId(userId);

        return generateInvalidCookieHeaders();
    }

    private HttpHeaders generateInvalidCookieHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(
                HttpHeaders.SET_COOKIE,
                httpCookieUtils.invalidateCookie(HttpCookieName.USER_ID, false).asSetCookieHeaderValue()
        );
        httpHeaders.add(
                HttpHeaders.SET_COOKIE,
                httpCookieUtils.invalidateCookie(HttpCookieName.ACCESS_TOKEN, false).asSetCookieHeaderValue()
        );
        httpHeaders.add(
                HttpHeaders.SET_COOKIE,
                httpCookieUtils.invalidateCookie(HttpCookieName.REFRESH_TOKEN, false).asSetCookieHeaderValue()
        );

        return httpHeaders;
    }
}
