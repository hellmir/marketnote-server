package com.personal.marketnote.notification.adapter.out.persistence.preference.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.preference.entity.NotificationPreferenceJpaEntity;
import com.personal.marketnote.notification.domain.template.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationPreferenceJpaRepository extends JpaRepository<NotificationPreferenceJpaEntity, Long> {
    List<NotificationPreferenceJpaEntity> findAllByUserIdAndStatus(Long userId, EntityStatus status);

    Optional<NotificationPreferenceJpaEntity> findByUserIdAndNotificationTypeAndStatus(Long userId, NotificationType notificationType, EntityStatus status);
}
