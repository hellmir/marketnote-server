package com.personal.marketnote.common.saga.exception;

public class SagaStepCompensationRequestNoValueException extends IllegalArgumentException {
    public SagaStepCompensationRequestNoValueException() {
        super("compensationRequest는 필수입니다.");
    }
}
