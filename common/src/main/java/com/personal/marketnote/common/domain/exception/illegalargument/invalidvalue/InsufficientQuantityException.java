package com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue;

public class InsufficientQuantityException extends InvalidValueException {
    private static final String MESSAGE = "재고 수량이 부족합니다. 현재 재고 수량: %d, 주문 수량: %d";

    public InsufficientQuantityException(Integer currentStock, Integer orderQuantity) {
        super(String.format(MESSAGE, currentStock, orderQuantity));
    }
}
