package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class PaymentEventNotFoundException extends DomainNotFoundException {
    private static final String PAYMENT_EVENT_NOT_FOUND_BY_ORDER_KEY = "ERR_PAYMENT_EVENT_01::거래 등록 정보를 찾을 수 없습니다. 주문 키: %s";

    public PaymentEventNotFoundException(String orderKey) {
        super(String.format(PAYMENT_EVENT_NOT_FOUND_BY_ORDER_KEY, orderKey));
    }
}
