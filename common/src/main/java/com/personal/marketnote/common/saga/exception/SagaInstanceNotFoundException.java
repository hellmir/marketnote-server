package com.personal.marketnote.common.saga.exception;

public class SagaInstanceNotFoundException extends RuntimeException {
    public SagaInstanceNotFoundException(Long id) {
        super("SagaInstance를 찾을 수 없습니다. id: " + id);
    }

    public SagaInstanceNotFoundException(String sagaId) {
        super("SagaInstance를 찾을 수 없습니다. sagaId: " + sagaId);
    }
}
