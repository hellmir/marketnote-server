package com.personal.marketnote.notification.domain.device;

public class DeviceTokenNotFoundException extends RuntimeException {
    public DeviceTokenNotFoundException(Long id) {
        super("ERR_DEVICE_TOKEN_03::디바이스 토큰을 찾을 수 없습니다. id=" + id);
    }

    public DeviceTokenNotFoundException() {
        super("ERR_DEVICE_TOKEN_03::디바이스 토큰을 찾을 수 없습니다.");
    }
}
