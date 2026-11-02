package com.personal.marketnote.common.utility.http.cookie;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class SpringResponseCookieHttpCookieObjectFactory implements HttpCookieObjectFactory {
    @Override
    public HttpCookieObject create(
            HttpCookieName httpCookieName, String domain, String value, long cookieAge, boolean httpOnly
    ) {
        return new SpringResponseCookieHttpCookieObject(
                ResponseCookie.from(httpCookieName.getCookieName())
                        .value(value)
                        .domain(domain)
                        .path("/")
                        .httpOnly(httpOnly)
                        .secure(true)
                        .maxAge(cookieAge)
                        .sameSite("None")
                        .build()
        );
    }
}
