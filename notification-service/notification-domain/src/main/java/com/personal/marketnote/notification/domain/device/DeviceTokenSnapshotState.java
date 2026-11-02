package com.personal.marketnote.notification.domain.device;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class DeviceTokenSnapshotState {
    private Long id;
    private Long userId;
    private String token;
    private Platform platform;
    private String deviceId;
    private LocalDateTime lastUsedAt;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
