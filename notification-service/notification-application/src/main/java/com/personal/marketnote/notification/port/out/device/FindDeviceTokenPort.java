package com.personal.marketnote.notification.port.out.device;

import com.personal.marketnote.notification.domain.device.DeviceToken;

import java.util.Optional;

public interface FindDeviceTokenPort {
    Optional<DeviceToken> findActiveByDeviceId(String deviceId);
}
