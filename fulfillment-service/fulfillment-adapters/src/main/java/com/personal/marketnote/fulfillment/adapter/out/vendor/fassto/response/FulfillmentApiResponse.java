package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.personal.marketnote.common.utility.FormatValidator;

import java.util.regex.Pattern;

public interface FulfillmentApiResponse {
    String INVALID_ACCESS_TOKEN_MARKER = "INVALID_ACCESS";
    Pattern INVALID_ACCESS_WORD_PATTERN =
            Pattern.compile("(?i)(^|[^A-Z0-9_])INVALID_ACCESS($|[^A-Z0-9_])");

    FulfillmentResponseHeader header();

    FulfillmentErrorInfo errorInfo();

    default String resolveErrorMessage() {
        return normalizeMessage(generateMessage());
    }

    default boolean isInvalidAccess() {
        if (equalsInvalidAccessMarker(extractErrorCode())
                || equalsInvalidAccessMarker(extractHeaderCode())) {
            return true;
        }
        return containsInvalidAccessMarker(extractErrorMessage())
                || containsInvalidAccessMarker(extractHeaderMessage());
    }

    private String extractErrorCode() {
        if (FormatValidator.hasNoValue(errorInfo())) {
            return null;
        }
        return errorInfo().errorCode();
    }

    private String extractErrorMessage() {
        if (FormatValidator.hasNoValue(errorInfo())) {
            return null;
        }
        return errorInfo().errorMessage();
    }

    private String extractHeaderCode() {
        if (FormatValidator.hasNoValue(header())) {
            return null;
        }
        return header().code();
    }

    private String extractHeaderMessage() {
        if (FormatValidator.hasNoValue(header())) {
            return null;
        }
        return header().msg();
    }

    static boolean equalsInvalidAccessMarker(String value) {
        if (FormatValidator.hasNoValue(value)) {
            return false;
        }
        return INVALID_ACCESS_TOKEN_MARKER.equalsIgnoreCase(value.trim());
    }

    static boolean containsInvalidAccessMarker(String value) {
        if (FormatValidator.hasNoValue(value)) {
            return false;
        }
        return INVALID_ACCESS_WORD_PATTERN.matcher(value).find();
    }

    private String generateMessage() {
        if (FormatValidator.hasValue(errorInfo()) && FormatValidator.hasValue(errorInfo().errorMessage())) {
            return errorInfo().errorMessage();
        }

        if (FormatValidator.hasValue(header()) && !header().isSuccess()) {
            return header().msg();
        }

        return null;
    }

    static String normalizeMessage(String message) {
        if (FormatValidator.hasNoValue(message)) {
            return null;
        }

        String trimmed = message.trim();
        if ("null".equalsIgnoreCase(trimmed)) {
            return null;
        }

        String lower = trimmed.toLowerCase();
        if (lower.startsWith("null:")) {
            return trimmed.substring("null:".length()).trim();
        }

        return trimmed;
    }
}
