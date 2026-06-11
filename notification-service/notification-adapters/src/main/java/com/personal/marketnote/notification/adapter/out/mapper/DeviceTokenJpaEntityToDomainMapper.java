package com.personal.marketnote.notification.adapter.out.mapper;

import com.personal.marketnote.notification.adapter.out.persistence.device.entity.DeviceTokenJpaEntity;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.DeviceTokenSnapshotState;

import java.util.Optional;

public class DeviceTokenJpaEntityToDomainMapper {

    private DeviceTokenJpaEntityToDomainMapper() {
    }

    public static Optional<DeviceToken> mapToDomain(DeviceTokenJpaEntity entity) {
        return Optional.ofNullable(entity)
                .map(e -> DeviceToken.from(
                        DeviceTokenSnapshotState.builder()
                                .id(e.getId())
                                .userId(e.getUserId())
                                .token(e.getToken())
                                .platform(e.getPlatform())
                                .deviceId(e.getDeviceId())
                                .lastUsedAt(e.getLastUsedAt())
                                .status(e.getStatus())
                                .createdAt(e.getCreatedAt())
                                .modifiedAt(e.getModifiedAt())
                                .build()
                ));
    }
}
