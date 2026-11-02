package com.personal.marketnote.common.utility.http.cookie;

import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HttpCookieUtils {
    private final HttpCookieObjectFactory httpCookieObjectFactory;
    private final String cookieDomain;

    public HttpCookieUtils(
            HttpCookieObjectFactory httpCookieObjectFactory, @Value("${client.cookie-domain}") String cookieDomain
    ) {
        this.httpCookieObjectFactory = httpCookieObjectFactory;
        this.cookieDomain = cookieDomain;
    }

    public HttpCookieObject generateHttpOnlyCookie(
            @Nonnull HttpCookieName httpCookieName, @Nonnull String value, long cookieAge
    ) {
        return generateCookie(
                httpCookieName,
                value,
                cookieAge,
                true
        );
    }

    public HttpCookieObject generateJsAccessibleCookie(
            @Nonnull HttpCookieName httpCookieName, @Nonnull String value, long cookieAge
    ) {
        return generateCookie(
                httpCookieName,
                value,
                cookieAge,
                false
        );
    }

    public HttpCookieObject invalidateCookie(HttpCookieName httpCookieName, boolean httpOnly) {
        return generateCookie(httpCookieName, "", 0, httpOnly);
    }

    private HttpCookieObject generateCookie(
            @Nonnull HttpCookieName httpCookieName, @Nonnull String value, long cookieAge, boolean httpOnly
    ) {
        return this.httpCookieObjectFactory.create(
                httpCookieName,
                this.cookieDomain,
                value,
                cookieAge,
                httpOnly
        );
    }
}
