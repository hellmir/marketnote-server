package com.personal.marketnote.common.saga.exception;

public class SagaSerializationException extends RuntimeException {
    public SagaSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
