package com.personal.marketnote.common.domain.exception.illegalargument.novalue;

public abstract class NoValueException extends IllegalArgumentException {
    public NoValueException(String message) {
        super(message);
    }
}
