package com.personal.marketnote.common.saga.exception;

public class SagaIdNoValueException extends IllegalArgumentException {
    public SagaIdNoValueException() {
        super("sagaId는 필수입니다.");
    }
}
