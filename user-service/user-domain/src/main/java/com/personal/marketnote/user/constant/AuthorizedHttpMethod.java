package com.personal.marketnote.user.constant;

public enum AuthorizedHttpMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    HEAD,
    OPTIONS,
    TRACE;

    public static AuthorizedHttpMethod parse(String value) {
        return AuthorizedHttpMethod.valueOf(value.strip().toUpperCase());
    }
}
