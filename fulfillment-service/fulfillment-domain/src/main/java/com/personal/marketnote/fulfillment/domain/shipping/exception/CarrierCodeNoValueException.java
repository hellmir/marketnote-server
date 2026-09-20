package com.personal.marketnote.fulfillment.domain.shipping.exception;

public class CarrierCodeNoValueException extends RuntimeException {
    private static final String MESSAGE = "ERR_SHIPPING_CARRIER_CODE_01::택배사 코드는 필수값입니다.";

    public CarrierCodeNoValueException() {
        super(MESSAGE);
    }
}
