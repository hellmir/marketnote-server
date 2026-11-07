package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class SettlementAlreadyExistsException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "이미 해당 기간의 정산이 존재합니다.";

    public SettlementAlreadyExistsException() {
        super(MESSAGE);
    }
}
