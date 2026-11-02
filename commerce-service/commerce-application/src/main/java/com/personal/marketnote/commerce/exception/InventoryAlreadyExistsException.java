package com.personal.marketnote.commerce.exception;

import lombok.Getter;

@Getter
public class InventoryAlreadyExistsException extends IllegalStateException {
    private static final String INVENTORY_ALREADY_EXISTS_EXCEPTION_MESSAGE
            = "이미 해당 가격 정책에 대한 재고가 존재합니다. 전송된 가격 정책 ID: %d";

    public InventoryAlreadyExistsException(Long pricePolicyId) {
        super(String.format(INVENTORY_ALREADY_EXISTS_EXCEPTION_MESSAGE, pricePolicyId));
    }
}
