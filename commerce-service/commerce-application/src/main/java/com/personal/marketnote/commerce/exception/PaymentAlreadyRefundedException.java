package com.personal.marketnote.commerce.exception;

public class PaymentAlreadyRefundedException extends RuntimeException {
    public PaymentAlreadyRefundedException(String orderKey) {
        super("ERR_PAYMENT_REFUND_01::이미 환불 처리된 결제입니다. orderKey=" + orderKey);
    }
}
