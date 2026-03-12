package com.personal.marketnote.commerce.exception;

public class DuplicateInventoryDeductionException extends RuntimeException {
    public DuplicateInventoryDeductionException(Long orderId) {
        super("이미 처리된 재고 차감입니다. orderId=" + orderId);
    }
}
