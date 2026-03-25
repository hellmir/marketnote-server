package com.personal.marketnote.common.saga.exception;

public class SagaStepRequestNoValueException extends IllegalArgumentException {
    public SagaStepRequestNoValueException() {
        super("request는 필수입니다.");
    }
}
