package com.personal.marketnote.common.saga.exception;

public class SagaDefinitionNotFoundException extends RuntimeException {
    public SagaDefinitionNotFoundException(String sagaType) {
        super("SagaDefinition을 찾을 수 없습니다. sagaType: " + sagaType);
    }
}
