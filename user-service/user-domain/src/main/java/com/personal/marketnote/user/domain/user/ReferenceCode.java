package com.personal.marketnote.user.domain.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.RegularExpressionConstant;
import com.personal.marketnote.user.domain.user.exception.InvalidReferenceCodeException;
import com.personal.marketnote.user.domain.user.exception.ReferenceCodeNoValueException;

import java.util.Objects;
import java.util.regex.Pattern;

public final class ReferenceCode {
    private static final String NO_VALUE_MESSAGE = "추천인 코드는 필수값입니다.";
    private static final String INVALID_FORMAT_MESSAGE = "추천인 코드 형식이 올바르지 않습니다.";
    private static final Pattern REFERENCE_CODE_PATTERN = Pattern.compile(RegularExpressionConstant.REFERENCE_CODE_PATTERN);
    private static final String MASKED_TOSTRING = "ReferenceCode{value=***}";

    private final String value;

    private ReferenceCode(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ReferenceCode of(String value) {
        validateNotBlank(value);
        validateFormat(value);
        return new ReferenceCode(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    private static void validateNotBlank(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new ReferenceCodeNoValueException(NO_VALUE_MESSAGE);
        }
    }

    private static void validateFormat(String value) {
        if (!REFERENCE_CODE_PATTERN.matcher(value).matches()) {
            throw new InvalidReferenceCodeException(INVALID_FORMAT_MESSAGE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReferenceCode other)) {
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
        return MASKED_TOSTRING;
    }
}
