package com.personal.marketnote.notification.domain.device;

public class InvalidPlatformException extends IllegalArgumentException {
    public InvalidPlatformException(String message) {
        super("ERR_DEVICE_TOKEN_01::" + message);
    }
}
