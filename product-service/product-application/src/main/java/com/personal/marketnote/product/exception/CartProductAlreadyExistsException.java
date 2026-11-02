package com.personal.marketnote.product.exception;

import lombok.Getter;

@Getter
public class CartProductAlreadyExistsException extends IllegalStateException {
    private static final String CART_PRODUCT_ALREADY_EXISTS_EXCEPTION_MESSAGE
            = "장바구니에 이미 해당 상품이 존재합니다. 전송된 회원 ID: %d, 가격 정책 ID: %d";

    public CartProductAlreadyExistsException(Long userId, Long policyId) {
        super(String.format(CART_PRODUCT_ALREADY_EXISTS_EXCEPTION_MESSAGE, userId, policyId));
    }
}
