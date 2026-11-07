package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class PaymentEventNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "ERR_PAYMENT_EVENT_01::거래 등록 정보를 찾을 수 없습니다.";

    public PaymentEventNotFoundException() {
        super(MESSAGE);
    }
}
