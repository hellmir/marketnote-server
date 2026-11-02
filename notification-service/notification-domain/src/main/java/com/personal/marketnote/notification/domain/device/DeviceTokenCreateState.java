package com.personal.marketnote.notification.domain.device;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class DeviceTokenCreateState {
    private Long userId;
    private String token;
    private Platform platform;
    private String deviceId;
    private LocalDateTime lastUsedAt;
}
