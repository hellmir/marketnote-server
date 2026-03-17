package com.personal.marketnote.product.exception;

public class InvalidPricePolicyAccumulatedPointException extends IllegalArgumentException {
    private static final String MESSAGE = "ERR_PRICE_POLICY_02::적립금이 현재 판매가를 초과할 수 없습니다.";

    public InvalidPricePolicyAccumulatedPointException() {
        super(MESSAGE);
    }
}
