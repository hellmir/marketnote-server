package com.personal.marketnote.common.domain.exception.token;

public class InvalidRefreshTokenException extends TokenException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }

    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
