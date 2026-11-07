package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class CartProductNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "장바구니 상품을 찾을 수 없습니다.";

    public CartProductNotFoundException() {
        super(MESSAGE);
    }
}
