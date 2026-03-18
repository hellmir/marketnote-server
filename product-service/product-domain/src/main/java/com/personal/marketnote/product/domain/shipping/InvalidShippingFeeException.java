package com.personal.marketnote.product.domain.shipping;

public class InvalidShippingFeeException extends IllegalArgumentException {
    private static final String MESSAGE = "ERR_SHIPPING_POLICY_01::배송비가 유효하지 않습니다. %s";

    public InvalidShippingFeeException(String detail) {
        super(String.format(MESSAGE, detail));
    }
}
