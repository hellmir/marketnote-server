package com.personal.marketnote.common.saga.exception;

public class SagaStepNameNoValueException extends IllegalArgumentException {
    public SagaStepNameNoValueException() {
        super("stepName은 필수입니다.");
    }
}
