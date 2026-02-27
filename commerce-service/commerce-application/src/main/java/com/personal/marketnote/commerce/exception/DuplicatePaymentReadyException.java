package com.personal.marketnote.commerce.exception;

public class DuplicatePaymentReadyException extends IllegalStateException {
    private static final String DUPLICATE_PAYMENT_READY_EXCEPTION_MESSAGE
            = "이미 거래 등록이 진행 중인 주문입니다. 주문 키: %s";

    public DuplicatePaymentReadyException(String orderKey) {
        super(String.format(DUPLICATE_PAYMENT_READY_EXCEPTION_MESSAGE, orderKey));
    }
}
