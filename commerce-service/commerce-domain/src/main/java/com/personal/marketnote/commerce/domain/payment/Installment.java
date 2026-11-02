package com.personal.marketnote.commerce.domain.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.Objects;

public final class Installment {
    private static final short LUMP_SUM = 0;
    private static final short MIN_INSTALLMENT_MONTHS = 2;
    private static final short MAX_INSTALLMENT_MONTHS = 36;
    private static final String INVALID_VALUE_MESSAGE =
            "할부 개월 수는 0(일시불) 또는 2~36 범위만 허용됩니다. value=%d";

    private final short value;

    private Installment(short value) {
        this.value = value;
    }

    @JsonCreator
    public static Installment of(short value) {
        validate(value);
        return new Installment(value);
    }

    public static Short toNullableValue(Installment installment) {
        if (FormatValidator.hasNoValue(installment)) {
            return null;
        }
        return installment.value;
    }

    @JsonValue
    public short getValue() {
        return value;
    }

    public boolean isLumpSum() {
        return value == LUMP_SUM;
    }

    private static void validate(short value) {
        if (value == LUMP_SUM) {
            return;
        }
        if (value < MIN_INSTALLMENT_MONTHS || value > MAX_INSTALLMENT_MONTHS) {
            throw new InvalidInstallmentException(String.format(INVALID_VALUE_MESSAGE, value));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Installment other)) {
            return false;
        }
        return value == other.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "Installment{value=" + value + "}";
    }
}
