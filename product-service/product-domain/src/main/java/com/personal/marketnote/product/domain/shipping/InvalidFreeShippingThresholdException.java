package com.personal.marketnote.product.domain.shipping;

public class InvalidFreeShippingThresholdException extends IllegalArgumentException {
    private static final String MESSAGE = "ERR_SHIPPING_POLICY_02::무료배송 기준금액이 유효하지 않습니다. %s";

    public InvalidFreeShippingThresholdException(String detail) {
        super(String.format(MESSAGE, detail));
    }
}
