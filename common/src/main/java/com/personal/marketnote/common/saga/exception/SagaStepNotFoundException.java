package com.personal.marketnote.common.saga.exception;

public class SagaStepNotFoundException extends RuntimeException {
    public SagaStepNotFoundException(Long id) {
        super("SagaStep을 찾을 수 없습니다. id: " + id);
    }
}
