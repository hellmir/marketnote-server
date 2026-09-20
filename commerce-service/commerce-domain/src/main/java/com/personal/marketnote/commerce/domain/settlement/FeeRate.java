package com.personal.marketnote.commerce.domain.settlement;

import com.personal.marketnote.common.utility.FormatValidator;

import java.util.Objects;

public class FeeRate {
    public static final int BASIS_POINT_DENOMINATOR = 10000;
    private static final String FEE_RATE_NEGATIVE_EXCEPTION = "수수료율은 0 이상이어야 합니다. value=%d";
    private static final String FEE_RATE_EXCEEDS_MAX_EXCEPTION = "수수료율은 %d(100%%) 이하여야 합니다. value=%d";

    private final int value;

    private FeeRate(int value) {
        this.value = value;
    }

    public static FeeRate of(int value) {
        validate(value);
        return new FeeRate(value);
    }

    private static void validate(int value) {
        if (value < 0) {
            throw new InvalidFeeRateException(String.format(FEE_RATE_NEGATIVE_EXCEPTION, value));
        }
        if (value > BASIS_POINT_DENOMINATOR) {
            throw new InvalidFeeRateException(String.format(FEE_RATE_EXCEEDS_MAX_EXCEPTION, BASIS_POINT_DENOMINATOR, value));
        }
    }

    public int getValue() {
        return value;
    }

    public double toPercentage() {
        return (double) value / BASIS_POINT_DENOMINATOR * 100;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (FormatValidator.hasNoValue(o) || getClass() != o.getClass()) {
            return false;
        }
        FeeRate feeRate = (FeeRate) o;
        return this.value == feeRate.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
