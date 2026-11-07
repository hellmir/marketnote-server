package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class SettlementPolicyAlreadyExistsException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "해당 판매자의 정산 정책이 이미 존재합니다.";

    public SettlementPolicyAlreadyExistsException() {
        super(MESSAGE);
    }
}
