package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class DuplicatePaymentReadyException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "이미 거래 등록이 진행 중인 주문입니다.";

    public DuplicatePaymentReadyException() {
        super(MESSAGE);
    }
}
