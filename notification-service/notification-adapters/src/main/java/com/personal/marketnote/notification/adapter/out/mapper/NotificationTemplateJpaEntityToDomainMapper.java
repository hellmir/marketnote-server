package com.personal.marketnote.notification.adapter.out.mapper;

import com.personal.marketnote.notification.adapter.out.persistence.template.entity.NotificationTemplateJpaEntity;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationTemplateSnapshotState;

import java.util.Optional;

public class NotificationTemplateJpaEntityToDomainMapper {

    private NotificationTemplateJpaEntityToDomainMapper() {
    }

    public static Optional<NotificationTemplate> mapToDomain(NotificationTemplateJpaEntity entity) {
        return Optional.ofNullable(entity)
                .map(e -> NotificationTemplate.from(
                        NotificationTemplateSnapshotState.builder()
                                .id(e.getId())
                                .templateCode(e.getTemplateCode())
                                .notificationType(e.getNotificationType())
                                .title(e.getTitle())
                                .bodyTemplate(e.getBodyTemplate())
                                .urlTemplate(e.getUrlTemplate())
                                .status(e.getStatus())
                                .createdAt(e.getCreatedAt())
                                .modifiedAt(e.getModifiedAt())
                                .build()
                ));
    }
}
