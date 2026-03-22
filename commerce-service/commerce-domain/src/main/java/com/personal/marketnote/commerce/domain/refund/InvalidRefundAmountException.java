package com.personal.marketnote.commerce.domain.refund;

public class InvalidRefundAmountException extends IllegalArgumentException {
    public InvalidRefundAmountException() {
        super("환불 금액은 0보다 커야 합니다");
    }
}
