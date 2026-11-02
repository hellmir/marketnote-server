package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class PaymentNotFoundException extends DomainNotFoundException {
    private static final String PAYMENT_NOT_FOUND_BY_ORDER_ID = "결제 정보를 찾을 수 없습니다. 주문 ID: %d";
    private static final String PAYMENT_NOT_FOUND_BY_ORDER_KEY = "결제 정보를 찾을 수 없습니다. 주문 키: %s";

    public PaymentNotFoundException(Long orderId) {
        super(String.format(PAYMENT_NOT_FOUND_BY_ORDER_ID, orderId));
    }

    public PaymentNotFoundException(String orderKey) {
        super(String.format(PAYMENT_NOT_FOUND_BY_ORDER_KEY, orderKey));
    }
}
