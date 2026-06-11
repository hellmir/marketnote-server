package com.personal.marketnote.notification.adapter.out.persistence.device.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.Platform;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_token", indexes = {
        @Index(name = "idx_device_token_device_id", columnList = "device_id", unique = true),
        @Index(name = "idx_device_token_user_id", columnList = "user_id")
})
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class DeviceTokenJpaEntity extends BaseGeneralEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, length = 4096)
    private String token;

    @Column(name = "platform", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Column(name = "device_id", nullable = false, unique = true, length = 200)
    private String deviceId;

    @Column(name = "last_used_at", nullable = false)
    private LocalDateTime lastUsedAt;

    public static DeviceTokenJpaEntity from(DeviceToken deviceToken) {
        return DeviceTokenJpaEntity.builder()
                .userId(deviceToken.getUserId())
                .token(deviceToken.getToken())
                .platform(deviceToken.getPlatform())
                .deviceId(deviceToken.getDeviceId())
                .lastUsedAt(deviceToken.getLastUsedAt())
                .build();
    }

    public void updateFrom(DeviceToken deviceToken) {
        if (deviceToken.isInactive()) {
            deactivate();
        }
        this.userId = deviceToken.getUserId();
        this.token = deviceToken.getToken();
        this.platform = deviceToken.getPlatform();
        this.lastUsedAt = deviceToken.getLastUsedAt();
    }
}
