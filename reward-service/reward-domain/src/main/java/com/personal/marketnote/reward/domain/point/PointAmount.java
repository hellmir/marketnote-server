package com.personal.marketnote.reward.domain.point;

import com.personal.marketnote.common.utility.FormatConverter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.exception.InvalidPointAmountException;
import com.personal.marketnote.reward.domain.exception.PointAmountNoValueException;

import java.util.regex.Pattern;

import static com.personal.marketnote.common.utility.RegularExpressionConstant.ZERO_OR_POSITIVE_INTEGER_PATTERN;

public final class PointAmount {
    private static final String POINT_AMOUNT_NO_VALUE_EXCEPTION_MESSAGE = "포인트 금액은 필수값입니다.";
    private static final String INVALID_POINT_AMOUNT_EXCEPTION_MESSAGE = "포인트 금액은 0 또는 양의 정수(0 이상의 숫자값)여야 합니다. 입력값: %s";
    private static final String SUBTRACT_NEGATIVE_RESULT_MESSAGE = "차감 결과가 음수입니다. 현재: %d, 차감: %d";

    private final long amount;

    private PointAmount(long amount) {
        this.amount = amount;
    }

    public static PointAmount of(String amount) {
        validate(amount);
        return new PointAmount(FormatConverter.parseToLong(amount));
    }

    public static PointAmount of(long amount) {
        if (amount < 0L) {
            throw new InvalidPointAmountException(String.format(INVALID_POINT_AMOUNT_EXCEPTION_MESSAGE, amount));
        }
        return new PointAmount(amount);
    }

    public static PointAmount zero() {
        return new PointAmount(0L);
    }

    private static void validate(String amount) {
        checkAmountIsNotBlank(amount);
        checkAmountPattern(amount);
    }

    private static void checkAmountIsNotBlank(String amount) {
        if (FormatValidator.hasNoValue(amount)) {
            throw new PointAmountNoValueException(POINT_AMOUNT_NO_VALUE_EXCEPTION_MESSAGE);
        }
    }

    private static void checkAmountPattern(String amount) {
        if (!FormatValidator.isValid(amount, Pattern.compile(ZERO_OR_POSITIVE_INTEGER_PATTERN))) {
            throw new InvalidPointAmountException(String.format(INVALID_POINT_AMOUNT_EXCEPTION_MESSAGE, amount));
        }
    }

    public static PointAmount generateChangedAmount(boolean isAccrual, PointAmount currentAmount, Long requestedAmount) {
        PointAmount addedAmount = PointAmount.of(
                String.valueOf(requestedAmount)
        );

        if (isAccrual) {
            return accumulate(currentAmount, addedAmount);
        }

        return reduce(currentAmount, addedAmount);
    }

    private static PointAmount accumulate(PointAmount currentAmount, PointAmount addedAmount) {
        return PointAmount.of(
                String.valueOf(currentAmount.getValue() + addedAmount.getValue())
        );
    }

    private static PointAmount reduce(PointAmount currentAmount, PointAmount reducedAmount) {
        return PointAmount.of(
                String.valueOf(currentAmount.getValue() - reducedAmount.getValue())
        );
    }

    public PointAmount add(PointAmount other) {
        return new PointAmount(Math.addExact(amount, other.amount));
    }

    public PointAmount subtract(PointAmount other) {
        long result = Math.subtractExact(amount, other.amount);
        if (result < 0L) {
            throw new InvalidPointAmountException(String.format(SUBTRACT_NEGATIVE_RESULT_MESSAGE, amount, other.amount));
        }
        return new PointAmount(result);
    }

    public boolean isZero() {
        return amount == 0L;
    }

    public boolean isPositive() {
        return amount > 0L;
    }

    public boolean isGreaterThan(PointAmount other) {
        return amount > other.amount;
    }

    public boolean isGreaterThanOrEqual(PointAmount other) {
        return amount >= other.amount;
    }

    public boolean isLessThan(PointAmount other) {
        return amount < other.amount;
    }

    public long getValue() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PointAmount other)) {
            return false;
        }
        return amount == other.amount;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(amount);
    }
}
