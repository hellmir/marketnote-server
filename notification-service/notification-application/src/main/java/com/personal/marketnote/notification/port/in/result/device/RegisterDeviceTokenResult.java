package com.personal.marketnote.notification.port.in.result.device;

public record RegisterDeviceTokenResult(Long id, boolean isNew) {

    public static RegisterDeviceTokenResult ofCreated(Long id) {
        return new RegisterDeviceTokenResult(id, true);
    }

    public static RegisterDeviceTokenResult ofUpdated(Long id) {
        return new RegisterDeviceTokenResult(id, false);
    }
}
