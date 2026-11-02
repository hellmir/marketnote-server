package com.personal.marketnote.common.domain.exception;

public class DomainAlreadyExistsException extends RuntimeException {
    public DomainAlreadyExistsException() {
        super();
    }

    public DomainAlreadyExistsException(String message) {
        super(message);
    }
}
