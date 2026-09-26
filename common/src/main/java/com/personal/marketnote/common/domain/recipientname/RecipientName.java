package com.personal.marketnote.common.domain.recipientname;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidRecipientNameException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.RecipientNameNoValueException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.RegularExpressionConstant;

import java.util.Objects;
import java.util.regex.Pattern;

public final class RecipientName {
    private static final String NO_VALUE_MESSAGE = "수령인명은 필수값입니다.";
    private static final String INVALID_FORMAT_MESSAGE = "수령인명 형식이 올바르지 않습니다.";
    private static final Pattern RECIPIENT_NAME_PATTERN = Pattern.compile(RegularExpressionConstant.RECIPIENT_NAME_PATTERN);
    private static final String MASKED_TOSTRING = "RecipientName{value=***}";

    private final String value;

    private RecipientName(String value) {
        this.value = value;
    }

    @JsonCreator
    public static RecipientName of(String value) {
        validateNotBlank(value);
        validateFormat(value);
        return new RecipientName(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    private static void validateNotBlank(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new RecipientNameNoValueException(NO_VALUE_MESSAGE);
        }
    }

    private static void validateFormat(String value) {
        if (!RECIPIENT_NAME_PATTERN.matcher(value).matches()) {
            throw new InvalidRecipientNameException(INVALID_FORMAT_MESSAGE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecipientName other)) {
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
