package com.personal.marketnote.product.domain.shipping;

public class InvalidIslandSurchargeException extends IllegalArgumentException {
    private static final String MESSAGE = "ERR_SHIPPING_POLICY_04::도서산간 추가 배송비가 유효하지 않습니다. %s";

    public InvalidIslandSurchargeException(String detail) {
        super(String.format(MESSAGE, detail));
    }
}
