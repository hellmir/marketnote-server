package com.personal.marketnote.user.exception;

public class DeliveryImpossibleAreaException extends RuntimeException {

    public DeliveryImpossibleAreaException(String address) {
        super("ERR_SHIPPING_ADDRESS_01::배송 불가 지역입니다. address=" + address);
    }
}
