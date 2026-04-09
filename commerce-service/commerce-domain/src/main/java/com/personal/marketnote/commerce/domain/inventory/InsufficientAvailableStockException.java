package com.personal.marketnote.commerce.domain.inventory;

public class InsufficientAvailableStockException extends IllegalArgumentException {
    private static final String MESSAGE = "가용재고가 부족합니다. 현재 가용재고: %d, 요청 수량: %d";

    public InsufficientAvailableStockException(int availableStock, int requestedQuantity) {
        super(String.format(MESSAGE, availableStock, requestedQuantity));
    }
}
