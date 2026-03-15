package com.personal.marketnote.commerce.exception;

public class DuplicateInventoryRestorationException extends RuntimeException {
    public DuplicateInventoryRestorationException(Long orderId) {
        super("이미 처리된 재고 복구입니다. orderId=" + orderId);
    }
}
