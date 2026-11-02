package com.personal.marketnote.reward.domain.exception;

public class InvalidRewardQuantityException extends IllegalArgumentException {
    private static final String MESSAGE = "보상 수량은 0 이상이어야 합니다. value=%d";

    public InvalidRewardQuantityException(long value) {
        super(String.format(MESSAGE, value));
    }
}
