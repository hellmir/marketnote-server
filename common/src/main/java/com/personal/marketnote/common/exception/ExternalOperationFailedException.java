package com.personal.marketnote.common.exception;

import lombok.Getter;

import java.io.IOException;
import java.io.UncheckedIOException;

@Getter
public abstract class ExternalOperationFailedException extends UncheckedIOException {
    public ExternalOperationFailedException(String message, IOException cause) {
        super(message, cause);
    }
}
