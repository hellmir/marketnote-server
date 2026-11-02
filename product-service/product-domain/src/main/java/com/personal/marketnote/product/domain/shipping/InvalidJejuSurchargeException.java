package com.personal.marketnote.product.domain.shipping;

public class InvalidJejuSurchargeException extends IllegalArgumentException {
    private static final String MESSAGE = "ERR_SHIPPING_POLICY_03::제주 추가 배송비가 유효하지 않습니다. %s";

    public InvalidJejuSurchargeException(String detail) {
        super(String.format(MESSAGE, detail));
    }
}
