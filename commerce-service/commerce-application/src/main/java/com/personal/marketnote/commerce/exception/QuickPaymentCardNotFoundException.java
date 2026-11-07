package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class QuickPaymentCardNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "빠른결제 카드를 찾을 수 없습니다.";

    public QuickPaymentCardNotFoundException() {
        super(MESSAGE);
    }
}
