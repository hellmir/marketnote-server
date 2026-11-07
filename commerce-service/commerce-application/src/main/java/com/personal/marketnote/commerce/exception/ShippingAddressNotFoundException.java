package com.personal.marketnote.commerce.exception;

public class ShippingAddressNotFoundException extends RuntimeException {
    private static final String MESSAGE = "ERR_SHIPPING_ADDRESS_01::배송지를 찾을 수 없습니다.";

    public ShippingAddressNotFoundException() {
        super(MESSAGE);
    }
}
