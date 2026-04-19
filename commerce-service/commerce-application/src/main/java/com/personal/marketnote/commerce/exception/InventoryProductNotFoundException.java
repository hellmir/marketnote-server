package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.Getter;

@Getter
public class InventoryProductNotFoundException extends DomainNotFoundException {
    private static final String INVENTORY_NOT_FOUND_EXCEPTION_MESSAGE = "재고를 찾을 수 없습니다. 전송된 상품 ID: %d";

    public InventoryProductNotFoundException(Long productId) {
        super(String.format(INVENTORY_NOT_FOUND_EXCEPTION_MESSAGE, productId));
    }
}
