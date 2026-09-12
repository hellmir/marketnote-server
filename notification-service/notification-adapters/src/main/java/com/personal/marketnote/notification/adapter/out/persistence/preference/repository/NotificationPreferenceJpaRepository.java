package com.personal.marketnote.notification.adapter.out.persistence.preference.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.preference.entity.NotificationPreferenceJpaEntity;
import com.personal.marketnote.notification.domain.template.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationPreferenceJpaRepository extends JpaRepository<NotificationPreferenceJpaEntity, Long> {
    List<NotificationPreferenceJpaEntity> findAllByUserIdAndStatus(Long userId, EntityStatus status);

    Optional<NotificationPreferenceJpaEntity> findByUserIdAndNotificationTypeAndStatus(Long userId, NotificationType notificationType, EntityStatus status);

    List<NotificationPreferenceJpaEntity> findByUserIdInAndNotificationTypeAndStatusAndEnabledTrue(
            List<Long> userIds, NotificationType notificationType, EntityStatus status);

    @Query("SELECT DISTINCT p.userId FROM NotificationPreferenceJpaEntity p WHERE p.status = 'ACTIVE'")
    List<Long> findAllDistinctUserIds();
}
