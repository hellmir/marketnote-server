package com.personal.marketnote.common.saga.exception;

public class SagaInstanceIdNoValueException extends IllegalArgumentException {
    public SagaInstanceIdNoValueException() {
        super("sagaInstanceId는 필수입니다.");
    }
}
