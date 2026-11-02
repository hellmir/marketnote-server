package com.personal.marketnote.fulfillment.exception;

public class ReturnSlipNumbersNoValueException extends IllegalArgumentException {
    public ReturnSlipNumbersNoValueException() {
        super("returnSlipNumbers는 필수입니다.");
    }
}
