package com.personal.marketnote.notification.domain.device;

public class DuplicateDeviceTokenException extends RuntimeException {
    public DuplicateDeviceTokenException(String deviceId) {
        super("ERR_DEVICE_TOKEN_04::이미 등록된 디바이스 ID입니다. deviceId=" + deviceId);
    }
}
