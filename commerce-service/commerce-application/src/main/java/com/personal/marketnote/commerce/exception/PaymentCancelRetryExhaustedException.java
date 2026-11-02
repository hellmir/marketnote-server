package com.personal.marketnote.commerce.exception;

public class PaymentCancelRetryExhaustedException extends RuntimeException {
    public PaymentCancelRetryExhaustedException(String orderKey, Throwable cause) {
        super("ERR_PAYMENT_CANCEL_01::결제 취소 재시도 소진. orderKey=" + orderKey, cause);
    }
}
