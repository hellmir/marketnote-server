package com.personal.marketnote.common.saga.exception;

public class SagaTypeNoValueException extends IllegalArgumentException {
    public SagaTypeNoValueException() {
        super("sagaType은 필수입니다.");
    }
}
