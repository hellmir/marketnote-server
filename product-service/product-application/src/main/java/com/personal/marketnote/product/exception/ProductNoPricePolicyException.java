package com.personal.marketnote.product.exception;

import lombok.Getter;

@Getter
public class ProductNoPricePolicyException extends IllegalStateException {
    private static final String PRODUCT_NO_PRICE_POLICY_EXCEPTION_MESSAGE = "상품에 가격 정책이 존재하지 않습니다. 대상 상품 ID: %d";

    public ProductNoPricePolicyException(Long productId) {
        super(String.format(PRODUCT_NO_PRICE_POLICY_EXCEPTION_MESSAGE, productId));
    }
}
