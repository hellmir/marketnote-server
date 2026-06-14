package com.personal.marketnote.notification.adapter.out.persistence.preference.repository;

import com.personal.marketnote.notification.adapter.out.persistence.preference.entity.NotificationPreferenceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceJpaRepository extends JpaRepository<NotificationPreferenceJpaEntity, Long> {
}
