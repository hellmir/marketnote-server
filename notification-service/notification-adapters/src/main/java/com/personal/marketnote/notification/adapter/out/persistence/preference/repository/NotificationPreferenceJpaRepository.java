package com.personal.marketnote.notification.adapter.out.persistence.preference.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.preference.entity.NotificationPreferenceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationPreferenceJpaRepository extends JpaRepository<NotificationPreferenceJpaEntity, Long> {
    List<NotificationPreferenceJpaEntity> findAllByUserIdAndStatus(Long userId, EntityStatus status);
}
