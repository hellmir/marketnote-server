package com.personal.marketnote.notification.port.out.device;

import com.personal.marketnote.notification.domain.device.DeviceToken;

public interface UpdateDeviceTokenPort {
    Long update(DeviceToken deviceToken);
}
