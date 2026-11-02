package com.personal.marketnote.notification.port.out.device;

import java.time.LocalDateTime;

public interface DeleteDeviceTokenPort {
    void deleteById(Long id);

    int deactivateStaleTokens(LocalDateTime threshold);
}
