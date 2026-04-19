package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.Getter;

@Getter
public class CartProductNotFoundException extends DomainNotFoundException {
    private static final String PRODUCT_NOT_FOUND_EXCEPTION_MESSAGE = "장바구니 상품을 찾을 수 없습니다. 전송된 가격 정책 ID: %d";

    public CartProductNotFoundException(Long pricePolicyId) {
        super(String.format(PRODUCT_NOT_FOUND_EXCEPTION_MESSAGE, pricePolicyId));
    }
}
