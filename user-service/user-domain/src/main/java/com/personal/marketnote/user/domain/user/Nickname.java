package com.personal.marketnote.user.domain.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.RegularExpressionConstant;
import com.personal.marketnote.user.domain.user.exception.InvalidNicknameException;
import com.personal.marketnote.user.domain.user.exception.NicknameNoValueException;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Nickname {
    private static final String NO_VALUE_MESSAGE = "닉네임은 필수값입니다.";
    private static final String INVALID_FORMAT_MESSAGE = "닉네임 형식이 올바르지 않습니다.";
    private static final Pattern NICKNAME_PATTERN = Pattern.compile(RegularExpressionConstant.NICKNAME_PATTERN);
    private static final String MASKED_TOSTRING = "Nickname{value=***}";

    private final String value;

    private Nickname(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Nickname of(String value) {
        validateNotBlank(value);
        validateFormat(value);
        return new Nickname(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    private static void validateNotBlank(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new NicknameNoValueException(NO_VALUE_MESSAGE);
        }
    }

    private static void validateFormat(String value) {
        if (!NICKNAME_PATTERN.matcher(value).matches()) {
            throw new InvalidNicknameException(INVALID_FORMAT_MESSAGE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Nickname other)) {
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
