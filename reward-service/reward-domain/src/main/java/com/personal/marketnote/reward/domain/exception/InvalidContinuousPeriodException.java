package com.personal.marketnote.reward.domain.exception;

public class InvalidContinuousPeriodException extends IllegalArgumentException {
    private static final String MESSAGE = "연속 출석일은 0보다 커야 합니다. value=%d";

    public InvalidContinuousPeriodException(short value) {
        super(String.format(MESSAGE, value));
    }
}
