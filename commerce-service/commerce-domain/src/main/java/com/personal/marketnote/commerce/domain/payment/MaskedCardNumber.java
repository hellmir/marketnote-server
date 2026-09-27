package com.personal.marketnote.commerce.domain.payment;

import com.personal.marketnote.common.utility.FormatValidator;

import java.util.Objects;
import java.util.regex.Pattern;

public final class MaskedCardNumber {
    private static final int MIN_LENGTH = 12;
    private static final int MAX_LENGTH = 20;
    private static final Pattern ALLOWED_PATTERN = Pattern.compile("^[0-9*\\-]{12,20}$");
    private static final String NO_VALUE_MESSAGE = "마스킹 카드번호는 필수값입니다.";
    private static final String INVALID_FORMAT_MESSAGE =
            "마스킹 카드번호 형식이 올바르지 않습니다. 길이 " + MIN_LENGTH + "~" + MAX_LENGTH + " 사이의 숫자/별표/하이픈만 허용됩니다.";
    private static final String MISSING_MASK_MESSAGE = "마스킹 카드번호에는 마스킹 문자(*)가 최소 1개 포함되어야 합니다.";

    private final String value;

    private MaskedCardNumber(String value) {
        this.value = value;
    }

    public static MaskedCardNumber of(String value) {
        validateNotBlank(value);
        validateFormat(value);
        validateMaskCharacter(value);
        return new MaskedCardNumber(value);
    }

    public static MaskedCardNumber fromNullable(String value) {
        if (FormatValidator.hasNoValue(value)) {
            return null;
        }
        return of(value);
    }

    public static String toNullableValue(MaskedCardNumber maskedCardNumber) {
        if (FormatValidator.hasNoValue(maskedCardNumber)) {
            return null;
        }
        return maskedCardNumber.value;
    }

    public String getValue() {
        return value;
    }

    private static void validateNotBlank(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new InvalidMaskedCardNumberException(NO_VALUE_MESSAGE);
        }
    }

    private static void validateFormat(String value) {
        if (!ALLOWED_PATTERN.matcher(value).matches()) {
            throw new InvalidMaskedCardNumberException(INVALID_FORMAT_MESSAGE);
        }
    }

    private static void validateMaskCharacter(String value) {
        if (value.indexOf('*') < 0) {
            throw new InvalidMaskedCardNumberException(MISSING_MASK_MESSAGE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MaskedCardNumber other)) {
            return false;
        }
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "MaskedCardNumber{value=***}";
    }
}
