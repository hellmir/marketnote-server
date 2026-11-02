package com.personal.marketnote.reward.domain.exception;

public class InsufficientPendingPointAmountException extends IllegalArgumentException {
    private static final String MESSAGE = "적립 예정 포인트가 부족합니다. 현재: %d, 요청: %d";

    public InsufficientPendingPointAmountException(long current, long requested) {
        super(String.format(MESSAGE, current, requested));
    }
}
