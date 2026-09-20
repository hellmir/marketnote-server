package com.personal.marketnote.product.domain.pricepolicy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.pricepolicy.exception.InvalidRateException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 할인율/적립률 등 percentage 단위 비율 값 객체.
 * <p>
 * 저장 포맷은 percentage(0 ~ 100.0)이며 소수 자리는 최대 1자리로 제한된다.
 * 예: 15.0 = 15%, 99.9 = 99.9%.
 */
public final class Rate {
    private static final BigDecimal MIN_VALUE = BigDecimal.ZERO;
    private static final BigDecimal MAX_VALUE = new BigDecimal("100");
    private static final int MAX_SCALE = 1;

    private final BigDecimal value;

    private Rate(BigDecimal value) {
        this.value = value;
    }

    @JsonCreator
    public static Rate of(BigDecimal value) {
        validateNotNull(value);
        validateScale(value);
        validateRange(value);
        return new Rate(value);
    }

    @JsonValue
    public BigDecimal getValue() {
        return value;
    }

    private static void validateNotNull(BigDecimal value) {
        if (FormatValidator.hasNoValue(value)) {
            throw new InvalidRateException("비율 값은 필수입니다.");
        }
    }

    private static void validateScale(BigDecimal value) {
        if (value.stripTrailingZeros().scale() > MAX_SCALE) {
            throw new InvalidRateException(
                    String.format("비율 값의 소수 자리는 최대 %d자리입니다. value=%s", MAX_SCALE, value.toPlainString())
            );
        }
    }

    private static void validateRange(BigDecimal value) {
        if (value.compareTo(MIN_VALUE) < 0 || value.compareTo(MAX_VALUE) > 0) {
            throw new InvalidRateException(
                    String.format("비율 값은 %s 이상 %s 이하여야 합니다. value=%s",
                            MIN_VALUE.toPlainString(), MAX_VALUE.toPlainString(), value.toPlainString())
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Rate other)) {
            return false;
        }
        return value.compareTo(other.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return "Rate{value=" + value.toPlainString() + "}";
    }
}
