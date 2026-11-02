package com.personal.marketnote.common.domain.email;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidEmailException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.EmailNoValueException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.RegularExpressionConstant;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {
    private static final String INVALID_FORMAT_MESSAGE = "이메일 형식이 올바르지 않습니다.";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(RegularExpressionConstant.EMAIL_PATTERN);
    private static final String MASKED_TOSTRING = "Email{value=***}";

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Email of(String value) {
        validateNotBlank(value);
        validateFormat(value);
        return new Email(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    private static void validateNotBlank(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new EmailNoValueException();
        }
    }

    private static void validateFormat(String value) {
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException(INVALID_FORMAT_MESSAGE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Email other)) {
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
