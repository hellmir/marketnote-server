package com.personal.marketnote.common.utility;

public final class PhoneNumberNormalizer {

    private static final String HYPHEN = "-";
    private static final String EMPTY = "";

    private PhoneNumberNormalizer() {
    }

    public static String normalize(String raw) {
        if (FormatValidator.hasNoValue(raw) || raw.isBlank()) {
            return raw;
        }
        return raw.replace(HYPHEN, EMPTY);
    }
}
