package com.personal.marketnote.commerce.exception;

public class InvalidCancelAmountException extends IllegalArgumentException {
    private static final String NON_POSITIVE_AMOUNT_MESSAGE
            = "ERR_PAYMENT_CANCEL_AMOUNT_01::부분취소 금액은 0보다 커야 합니다.";
    private static final String EXCEEDS_REFUNDABLE_MESSAGE
            = "ERR_PAYMENT_CANCEL_AMOUNT_02::취소 금액이 환불 가능 금액을 초과합니다.";

    private InvalidCancelAmountException(String message) {
        super(message);
    }

    public static InvalidCancelAmountException nonPositiveAmount() {
        return new InvalidCancelAmountException(NON_POSITIVE_AMOUNT_MESSAGE);
    }

    public static InvalidCancelAmountException exceedsRefundable() {
        return new InvalidCancelAmountException(EXCEEDS_REFUNDABLE_MESSAGE);
    }
}
