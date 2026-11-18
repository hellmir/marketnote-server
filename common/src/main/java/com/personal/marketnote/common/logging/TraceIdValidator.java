package com.personal.marketnote.common.logging;

import com.personal.marketnote.common.utility.FormatValidator;

import java.util.UUID;
import java.util.regex.Pattern;

public final class TraceIdValidator {

    public static final Pattern TRACE_ID_PATTERN = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    private TraceIdValidator() {
    }

    public static String validateOrFallback(String candidate) {
        if (FormatValidator.hasValue(candidate) && TRACE_ID_PATTERN.matcher(candidate).matches()) {
            return candidate;
        }
        return UUID.randomUUID().toString();
    }
}
