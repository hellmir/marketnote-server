package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class CartProductAlreadyExistsException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "장바구니에 이미 해당 상품이 존재합니다.";

    public CartProductAlreadyExistsException() {
        super(MESSAGE);
    }
}
