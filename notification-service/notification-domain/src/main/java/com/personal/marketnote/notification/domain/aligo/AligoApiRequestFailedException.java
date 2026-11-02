package com.personal.marketnote.notification.domain.aligo;

public class AligoApiRequestFailedException extends AligoApiException {

    public AligoApiRequestFailedException(String message) {
        super("ERR_ALIGO_06::" + message);
    }

    public AligoApiRequestFailedException(String message, Throwable cause) {
        super("ERR_ALIGO_06::" + message, cause);
    }
}
