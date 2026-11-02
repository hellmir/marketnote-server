package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class InventoryProductNotFoundException extends DomainNotFoundException {
    private static final String INVENTORY_NOT_FOUND_MESSAGE = "재고를 찾을 수 없습니다.";

    public InventoryProductNotFoundException() {
        super(INVENTORY_NOT_FOUND_MESSAGE);
    }
}
