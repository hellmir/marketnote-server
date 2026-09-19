package com.personal.marketnote.common.domain.money;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidMoneyAmountException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.MoneyAmountNoValueException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.Objects;

public final class Money {
    private static final String NO_VALUE_MESSAGE = "금액은 필수값입니다.";
    private static final String INVALID_AMOUNT_MESSAGE = "금액은 0 이상이어야 합니다. 입력값: %d";
    private static final String SUBTRACT_NEGATIVE_RESULT_MESSAGE = "차감 결과가 음수입니다. 현재: %d, 차감: %d";

    private final long amount;

    private Money(long amount) {
        this.amount = amount;
    }

    @JsonCreator
    public static Money of(long amount) {
        validateNotNegative(amount);
        return new Money(amount);
    }

    public static Money of(Long amount) {
        validateNotNull(amount);
        validateNotNegative(amount);
        return new Money(amount);
    }

    public static Money zero() {
        return new Money(0);
    }

    public Money add(Money other) {
        long result = Math.addExact(this.amount, other.amount);
        return new Money(result);
    }

    public Money subtract(Money other) {
        long result = Math.subtractExact(this.amount, other.amount);
        if (result < 0) {
            throw new InvalidMoneyAmountException(
                    String.format(SUBTRACT_NEGATIVE_RESULT_MESSAGE, this.amount, other.amount)
            );
        }
        return new Money(result);
    }

    public Money multiply(long multiplier) {
        long result = Math.multiplyExact(this.amount, multiplier);
        return new Money(result);
    }

    public boolean isZero() {
        return amount == 0;
    }

    public boolean isPositive() {
        return amount > 0;
    }

    public boolean isGreaterThan(Money other) {
        return this.amount > other.amount;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount >= other.amount;
    }

    public boolean isLessThan(Money other) {
        return this.amount < other.amount;
    }

    @JsonValue
    public long getValue() {
        return amount;
    }

    private static void validateNotNull(Long amount) {
        if (FormatValidator.hasNoValue(amount)) {
            throw new MoneyAmountNoValueException(NO_VALUE_MESSAGE);
        }
    }

    private static void validateNotNegative(long amount) {
        if (amount < 0) {
            throw new InvalidMoneyAmountException(String.format(INVALID_AMOUNT_MESSAGE, amount));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money other)) {
            return false;
        }
        return amount == other.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(amount);
    }

    @Override
    public String toString() {
        return "Money{amount=" + amount + "}";
    }
}
