package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.notification.domain.device.DeviceTokenCreateState;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.port.in.command.RegisterDeviceTokenCommand;

import java.time.LocalDateTime;

public class DeviceTokenCommandToStateMapper {

    private DeviceTokenCommandToStateMapper() {
    }

    public static DeviceTokenCreateState mapToState(
            RegisterDeviceTokenCommand command, Platform platform, LocalDateTime lastUsedAt
    ) {
        return DeviceTokenCreateState.builder()
                .userId(command.userId())
                .token(command.token())
                .platform(platform)
                .deviceId(command.deviceId())
                .lastUsedAt(lastUsedAt)
                .build();
    }
}
