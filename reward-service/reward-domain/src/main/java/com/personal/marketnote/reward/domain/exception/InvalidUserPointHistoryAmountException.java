package com.personal.marketnote.reward.domain.exception;

public class InvalidUserPointHistoryAmountException extends IllegalArgumentException {
    private static final String MESSAGE = "포인트 이력의 금액은 0 이상이어야 합니다. amount=%s";

    public InvalidUserPointHistoryAmountException(Long amount) {
        super(String.format(MESSAGE, amount));
    }
}
