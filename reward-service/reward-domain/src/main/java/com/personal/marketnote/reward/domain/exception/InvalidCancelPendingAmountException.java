package com.personal.marketnote.reward.domain.exception;

public class InvalidCancelPendingAmountException extends IllegalStateException {
    private static final String MESSAGE = "취소 대상 적립 예정 포인트 합계는 0보다 커야 합니다. totalAmount=%d";

    public InvalidCancelPendingAmountException(Long totalAmount) {
        super(String.format(MESSAGE, totalAmount));
    }
}
