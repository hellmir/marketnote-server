package com.personal.marketnote.notification.domain.aligo;

public abstract class AligoApiException extends RuntimeException {

    protected AligoApiException(String message) {
        super(message);
    }

    protected AligoApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
