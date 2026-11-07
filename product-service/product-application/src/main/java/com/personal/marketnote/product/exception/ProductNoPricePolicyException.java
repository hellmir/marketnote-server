package com.personal.marketnote.product.exception;

public class ProductNoPricePolicyException extends IllegalStateException {
    private static final String MESSAGE = "ERR_PRODUCT_PRICE_POLICY_01::상품에 가격 정책이 존재하지 않습니다.";

    public ProductNoPricePolicyException() {
        super(MESSAGE);
    }
}
