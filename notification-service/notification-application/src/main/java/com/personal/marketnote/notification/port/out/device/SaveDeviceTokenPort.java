package com.personal.marketnote.notification.port.out.device;

import com.personal.marketnote.notification.domain.device.DeviceToken;

public interface SaveDeviceTokenPort {
    Long save(DeviceToken deviceToken);
}
