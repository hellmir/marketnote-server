package com.personal.marketnote.common.domain.quantity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidQuantityException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.QuantityNoValueException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.Objects;

public final class Quantity {
    private static final String NO_VALUE_MESSAGE = "수량은 필수값입니다.";
    private static final String INVALID_VALUE_MESSAGE = "수량은 1 이상이어야 합니다. 입력값: %d";
    private static final String SUBTRACT_BELOW_ONE_MESSAGE = "차감 결과가 1 미만입니다. 현재: %d, 차감: %d";
    private static final String MULTIPLY_BELOW_ONE_MESSAGE = "곱셈 결과가 1 미만입니다. 현재: %d, 배수: %d";

    private final int value;

    private Quantity(int value) {
        this.value = value;
    }

    @JsonCreator
    public static Quantity of(int value) {
        validateAtLeastOne(value);
        return new Quantity(value);
    }

    public static Quantity of(Integer value) {
        validateNotNull(value);
        validateAtLeastOne(value);
        return new Quantity(value);
    }

    public Quantity add(Quantity other) {
        int result = Math.addExact(this.value, other.value);
        return new Quantity(result);
    }

    public Quantity subtract(Quantity other) {
        int result = Math.subtractExact(this.value, other.value);
        if (result < 1) {
            throw new InvalidQuantityException(
                    String.format(SUBTRACT_BELOW_ONE_MESSAGE, this.value, other.value)
            );
        }
        return new Quantity(result);
    }

    public Quantity multiply(int multiplier) {
        int result = Math.multiplyExact(this.value, multiplier);
        if (result < 1) {
            throw new InvalidQuantityException(
                    String.format(MULTIPLY_BELOW_ONE_MESSAGE, this.value, multiplier)
            );
        }
        return new Quantity(result);
    }

    public boolean isGreaterThan(Quantity other) {
        return this.value > other.value;
    }

    public boolean isGreaterThanOrEqual(Quantity other) {
        return this.value >= other.value;
    }

    public boolean isLessThan(Quantity other) {
        return this.value < other.value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    private static void validateNotNull(Integer value) {
        if (FormatValidator.hasNoValue(value)) {
            throw new QuantityNoValueException(NO_VALUE_MESSAGE);
        }
    }

    private static void validateAtLeastOne(int value) {
        if (value < 1) {
            throw new InvalidQuantityException(String.format(INVALID_VALUE_MESSAGE, value));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Quantity other)) {
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
        return "Quantity{value=" + value + "}";
    }
}
