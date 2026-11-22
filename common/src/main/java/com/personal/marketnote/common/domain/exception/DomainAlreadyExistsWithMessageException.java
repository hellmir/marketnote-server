package com.personal.marketnote.common.domain.exception;

public abstract class DomainAlreadyExistsWithMessageException extends DomainAlreadyExistsException {

    protected DomainAlreadyExistsWithMessageException(String message) {
        super(message);
    }

    protected DomainAlreadyExistsWithMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
