package com.personal.marketnote.common.domain.phonenumber;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidPhoneNumberException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.PhoneNumberNoValueException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.RegularExpressionConstant;

import java.util.Objects;
import java.util.regex.Pattern;

public final class PhoneNumber {
    private static final String NO_VALUE_MESSAGE = "전화번호는 필수값입니다.";
    private static final String INVALID_FORMAT_MESSAGE = "전화번호 형식이 올바르지 않습니다.";
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile(RegularExpressionConstant.PHONE_NUMBER_PATTERN);
    private static final String MASKED_TOSTRING = "PhoneNumber{value=***}";

    private final String value;

    private PhoneNumber(String value) {
        this.value = value;
    }

    @JsonCreator
    public static PhoneNumber of(String value) {
        validateNotBlank(value);
        validateFormat(value);
        return new PhoneNumber(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    private static void validateNotBlank(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new PhoneNumberNoValueException(NO_VALUE_MESSAGE);
        }
    }

    private static void validateFormat(String value) {
        if (!PHONE_NUMBER_PATTERN.matcher(value).matches()) {
            throw new InvalidPhoneNumberException(INVALID_FORMAT_MESSAGE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PhoneNumber other)) {
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
