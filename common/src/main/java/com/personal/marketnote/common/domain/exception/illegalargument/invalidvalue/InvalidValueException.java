package com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue;

public abstract class InvalidValueException extends IllegalArgumentException {
    public InvalidValueException(String message) {
        super(message);
    }

    public InvalidValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
