package com.personal.marketnote.common.domain.exception.token;

public class UnsupportedCodeException extends TokenException {
    public UnsupportedCodeException(String message) {
        super(message);
    }

    public UnsupportedCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
