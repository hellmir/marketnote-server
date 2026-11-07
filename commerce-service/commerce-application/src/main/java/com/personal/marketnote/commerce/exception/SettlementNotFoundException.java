package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class SettlementNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "정산을 찾을 수 없습니다.";

    public SettlementNotFoundException() {
        super(MESSAGE);
    }
}
