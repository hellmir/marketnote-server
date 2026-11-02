package com.personal.marketnote.reward.domain.exception;

public class InvalidConfirmPendingAmountException extends IllegalStateException {
    private static final String MESSAGE = "확정 대상 적립 예정 포인트 합계는 0보다 커야 합니다. totalAmount=%d";

    public InvalidConfirmPendingAmountException(Long totalAmount) {
        super(String.format(MESSAGE, totalAmount));
    }
}
