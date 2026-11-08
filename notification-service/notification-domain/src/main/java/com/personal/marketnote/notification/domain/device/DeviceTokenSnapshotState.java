package com.personal.marketnote.notification.domain.device;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeviceTokenSnapshotState {
    private final Long id;
    private final Long userId;
    private final String token;
    private final Platform platform;
    private final String deviceId;
    private final LocalDateTime lastUsedAt;
    private final EntityStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
