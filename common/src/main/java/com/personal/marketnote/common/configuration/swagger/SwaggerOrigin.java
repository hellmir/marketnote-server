package com.personal.marketnote.common.configuration.swagger;

import com.personal.marketnote.common.exception.InvalidSwaggerOriginException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.net.URI;
import java.util.Set;

public record SwaggerOrigin(String scheme, String baseHost) {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    public SwaggerOrigin {
        if (FormatValidator.hasNoValue(scheme) || !ALLOWED_SCHEMES.contains(scheme)) {
            throw new InvalidSwaggerOriginException(scheme + "://" + baseHost);
        }
        if (FormatValidator.hasNoValue(baseHost)) {
            throw new InvalidSwaggerOriginException(scheme + "://" + baseHost);
        }
    }

    public static SwaggerOrigin parse(String origin) {
        if (FormatValidator.hasNoValue(origin)) {
            throw new InvalidSwaggerOriginException(origin);
        }

        URI uri = parseUri(origin);
        String parsedScheme = uri.getScheme();
        if (FormatValidator.hasNoValue(parsedScheme) || !ALLOWED_SCHEMES.contains(parsedScheme)) {
            throw new InvalidSwaggerOriginException(origin);
        }

        String host = uri.getHost();
        if (FormatValidator.hasNoValue(host)) {
            throw new InvalidSwaggerOriginException(origin);
        }

        int port = uri.getPort();
        String resolvedHost = port < 0 ? host : host + ":" + port;
        return new SwaggerOrigin(parsedScheme, resolvedHost);
    }

    private static URI parseUri(String origin) {
        try {
            return URI.create(origin);
        } catch (IllegalArgumentException iae) {
            throw new InvalidSwaggerOriginException(origin);
        }
    }
}
