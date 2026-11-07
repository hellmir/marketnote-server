package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class InventoryAlreadyExistsException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "이미 해당 가격 정책에 대한 재고가 존재합니다.";

    public InventoryAlreadyExistsException() {
        super(MESSAGE);
    }
}
