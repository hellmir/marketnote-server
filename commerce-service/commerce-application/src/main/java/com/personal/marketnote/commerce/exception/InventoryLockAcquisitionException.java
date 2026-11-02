package com.personal.marketnote.commerce.exception;

public class InventoryLockAcquisitionException extends RuntimeException {
    private static final String MESSAGE = "재고 락을 획득하지 못했습니다. 가격 정책 ID: %d";

    public InventoryLockAcquisitionException(Long pricePolicyId) {
        super(String.format(MESSAGE, pricePolicyId));
    }
}

