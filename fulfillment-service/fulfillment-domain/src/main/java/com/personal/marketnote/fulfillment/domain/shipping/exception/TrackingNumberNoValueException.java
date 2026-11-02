package com.personal.marketnote.fulfillment.domain.shipping.exception;

public class TrackingNumberNoValueException extends RuntimeException {
    private static final String MESSAGE = "ERR_SHIPPING_TRACKING_NUMBER_01::운송장번호는 필수값입니다.";

    public TrackingNumberNoValueException() {
        super(MESSAGE);
    }
}
