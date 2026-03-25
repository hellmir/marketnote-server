package com.personal.marketnote.common.saga.exception;

public class InvalidSagaStepIndexException extends IllegalArgumentException {
    public InvalidSagaStepIndexException(int stepIndex) {
        super("stepIndex는 0 이상이어야 합니다. stepIndex: " + stepIndex);
    }
}
