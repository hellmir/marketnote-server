package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.exception.BrandCodeNoValueException;

import java.util.Objects;

public final class BrandCode {
    private static final String NO_VALUE_MESSAGE = "브랜드 코드는 필수값입니다.";

    private final String value;

    private BrandCode(String value) {
        this.value = value;
    }

    public static BrandCode of(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new BrandCodeNoValueException(NO_VALUE_MESSAGE);
        }
        return new BrandCode(value);
    }

    public static BrandCode fromSnapshot(String value) {
        return new BrandCode(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BrandCode other)) {
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
        return "BrandCode{value=" + value + "}";
    }
}
