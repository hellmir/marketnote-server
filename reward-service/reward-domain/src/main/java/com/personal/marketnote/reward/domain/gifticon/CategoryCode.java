package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.exception.CategoryCodeNoValueException;

import java.util.Objects;

public final class CategoryCode {
    private static final String NO_VALUE_MESSAGE = "카테고리 코드는 필수값입니다.";

    private final String value;

    private CategoryCode(String value) {
        this.value = value;
    }

    public static CategoryCode of(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new CategoryCodeNoValueException(NO_VALUE_MESSAGE);
        }
        return new CategoryCode(value);
    }

    public static CategoryCode fromSnapshot(String value) {
        return new CategoryCode(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CategoryCode other)) {
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
        return "CategoryCode{value=" + value + "}";
    }
}
