package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.personal.marketnote.common.utility.FormatValidator;

public interface FulfillmentApiResponse {
    FulfillmentResponseHeader header();

    FulfillmentErrorInfo errorInfo();

    default String resolveErrorMessage() {
        return normalizeMessage(generateMessage());
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
