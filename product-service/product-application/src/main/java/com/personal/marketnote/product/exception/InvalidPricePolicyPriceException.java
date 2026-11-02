package com.personal.marketnote.product.exception;

public class InvalidPricePolicyPriceException extends IllegalArgumentException {
    private static final String MESSAGE = "ERR_PRICE_POLICY_01::현재 판매가가 정가를 초과할 수 없습니다.";

    public InvalidPricePolicyPriceException() {
        super(MESSAGE);
    }
}
