package com.personal.marketnote.notification.domain.notification;

public class FcmSendFailedException extends RuntimeException {

    public FcmSendFailedException(String message) {
        super("ERR_FCM_01::" + message);
    }

    public FcmSendFailedException(String message, Throwable cause) {
        super("ERR_FCM_01::" + message, cause);
    }
}
