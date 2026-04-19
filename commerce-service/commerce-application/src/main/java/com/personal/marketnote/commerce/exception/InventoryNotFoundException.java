package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.Getter;

@Getter
public class InventoryNotFoundException extends DomainNotFoundException {
    private static final String INVENTORY_NOT_FOUND_EXCEPTION_MESSAGE = "재고를 찾을 수 없습니다. 전송된 가격 정책 ID: %d";

    public InventoryNotFoundException(Long pricePolicyId) {
        super(String.format(INVENTORY_NOT_FOUND_EXCEPTION_MESSAGE, pricePolicyId));
    }
}
