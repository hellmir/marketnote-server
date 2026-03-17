package com.personal.marketnote.product.domain.cart;

public class InvalidCartProductQuantityException extends IllegalArgumentException {
    private static final String MESSAGE = "ERR_CART_01::장바구니 수량이 유효하지 않습니다. %s";

    public InvalidCartProductQuantityException(String detail) {
        super(String.format(MESSAGE, detail));
    }
}
