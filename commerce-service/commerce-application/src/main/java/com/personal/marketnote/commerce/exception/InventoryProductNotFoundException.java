package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.Getter;

import java.util.UUID;

@Getter
public class InventoryProductNotFoundException extends DomainNotFoundException {
    private static final String INVENTORY_NOT_FOUND_BY_PRODUCT_ID_MESSAGE = "재고를 찾을 수 없습니다. 전송된 상품 ID: %d";
    private static final String INVENTORY_NOT_FOUND_BY_PRODUCT_KEY_MESSAGE = "재고를 찾을 수 없습니다. 전송된 상품 키: %s";

    public InventoryProductNotFoundException(Long productId) {
        super(String.format(INVENTORY_NOT_FOUND_BY_PRODUCT_ID_MESSAGE, productId));
    }

    public InventoryProductNotFoundException(UUID productKey) {
        super(String.format(INVENTORY_NOT_FOUND_BY_PRODUCT_KEY_MESSAGE, productKey));
    }
}
