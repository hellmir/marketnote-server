package com.personal.marketnote.common.domain.exception.token;

public class InvalidAccessTokenException extends TokenException {
    public InvalidAccessTokenException(String message) {
        super(message);
    }

    public InvalidAccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
