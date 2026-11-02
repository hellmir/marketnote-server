package com.personal.marketnote.common.utility;

import java.util.Collection;
import java.util.regex.Pattern;

public class FormatValidator {
    public static boolean isValid(CharSequence value, Pattern pattern) {
        return hasValue(value) && hasValue(pattern) && pattern.matcher(value).matches();
    }

    public static boolean hasNoValue(Object value) {
        return !hasValue(value);
    }

    public static boolean hasValue(Object value) {
        return value != null && !value.toString().isBlank();
    }

    public static boolean hasNoValue(Collection<?> value) {
        return !hasValue(value);
    }

    public static boolean hasValue(Collection<?> value) {
        return value != null && !value.isEmpty();
    }

    public static boolean hasNoValue(Number value) {
        return !hasValue(value);
    }

    public static boolean hasValue(Number value) {
        return value != null;
    }

    public static boolean notEquals(Object value1, Object value2) {
        return !equals(value1, value2);
    }

    public static boolean equals(Object value1, Object value2) {
        return hasValue(value1) && hasValue(value2) && value1.equals(value2);
    }

    public static boolean notEqualsIgnoreCase(String value1, String value2) {
        return !equalsIgnoreCase(value1, value2);
    }

    public static boolean equalsIgnoreCase(String value1, String value2) {
        return hasValue(value1) && hasValue(value2) && value1.equalsIgnoreCase(value2);
    }

    public static boolean containsKeyword(String target, String keyword) {
        return target.toLowerCase().contains(keyword.toLowerCase());
    }
}
