package com.personal.marketnote.notification.domain.device;

public class InvalidDeviceTokenException extends IllegalArgumentException {
    public InvalidDeviceTokenException(String message) {
        super("ERR_DEVICE_TOKEN_02::" + message);
    }
}
