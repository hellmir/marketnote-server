package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.reward.domain.exception.InvalidValidDaysException;

import java.util.Objects;

public final class ValidDays {
    private static final String INVALID_MESSAGE = "쿠폰 유효일은 1일 이상이어야 합니다.";

    private final int value;

    private ValidDays(int value) {
        this.value = value;
    }

    public static ValidDays of(int value) {
        if (value <= 0) {
            throw new InvalidValidDaysException(INVALID_MESSAGE);
        }
        return new ValidDays(value);
    }

    public static ValidDays fromSnapshot(int value) {
        return new ValidDays(value);
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValidDays other)) {
            return false;
        }
        return value == other.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "ValidDays{value=" + value + "}";
    }
}
