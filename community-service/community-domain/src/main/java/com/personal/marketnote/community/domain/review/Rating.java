package com.personal.marketnote.community.domain.review;

import com.personal.marketnote.community.domain.review.exception.InvalidRatingPointException;

import java.util.Objects;

public final class Rating {
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 5;

    private final int value;

    private Rating(int value) {
        this.value = value;
    }

    public static Rating of(float value) {
        validateInteger(value);
        int intValue = (int) value;
        validateRange(intValue);
        return new Rating(intValue);
    }

    public int getValue() {
        return value;
    }

    private static void validateInteger(float value) {
        if (value != Math.floor(value)) {
            throw new InvalidRatingPointException((int) value);
        }
    }

    private static void validateRange(int value) {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new InvalidRatingPointException(value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Rating other)) {
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
        return "Rating{value=" + value + "}";
    }
}
