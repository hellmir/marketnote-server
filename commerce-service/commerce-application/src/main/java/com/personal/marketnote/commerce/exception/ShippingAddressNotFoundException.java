package com.personal.marketnote.commerce.exception;

public class ShippingAddressNotFoundException extends RuntimeException {
    public ShippingAddressNotFoundException(Long shippingAddressId) {
        super("ERR_SHIPPING_ADDRESS_01::배송지를 찾을 수 없습니다. shippingAddressId=" + shippingAddressId);
    }
}
