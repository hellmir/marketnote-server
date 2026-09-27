package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.exception.GoodsCodeNoValueException;

import java.util.Objects;

public final class GoodsCode {
    private static final String NO_VALUE_MESSAGE = "상품 코드는 필수값입니다.";

    private final String value;

    private GoodsCode(String value) {
        this.value = value;
    }

    public static GoodsCode of(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            throw new GoodsCodeNoValueException(NO_VALUE_MESSAGE);
        }
        return new GoodsCode(value);
    }

    public static GoodsCode fromSnapshot(String value) {
        return new GoodsCode(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GoodsCode other)) {
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
        return "GoodsCode{value=" + value + "}";
    }
}
