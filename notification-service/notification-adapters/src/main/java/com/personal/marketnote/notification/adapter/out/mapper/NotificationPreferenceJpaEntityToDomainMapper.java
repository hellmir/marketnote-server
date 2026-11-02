package com.personal.marketnote.notification.adapter.out.mapper;

import com.personal.marketnote.notification.adapter.out.persistence.preference.entity.NotificationPreferenceJpaEntity;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.preference.NotificationPreferenceSnapshotState;

import java.util.Optional;

public class NotificationPreferenceJpaEntityToDomainMapper {

    private NotificationPreferenceJpaEntityToDomainMapper() {
    }

    public static Optional<NotificationPreference> mapToDomain(NotificationPreferenceJpaEntity entity) {
        return Optional.ofNullable(entity)
                .map(e -> NotificationPreference.from(
                        NotificationPreferenceSnapshotState.builder()
                                .id(e.getId())
                                .userId(e.getUserId())
                                .notificationType(e.getNotificationType())
                                .enabled(e.isEnabled())
                                .consentedAt(e.getConsentedAt())
                                .status(e.getStatus())
                                .createdAt(e.getCreatedAt())
                                .modifiedAt(e.getModifiedAt())
                                .build()
                ));
    }
}
