package com.personal.marketnote.notification.adapter.out.persistence.vendorcommunication.repository;

import com.personal.marketnote.notification.adapter.out.persistence.vendorcommunication.entity.NotificationVendorCommunicationHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationVendorCommunicationHistoryJpaRepository
        extends JpaRepository<NotificationVendorCommunicationHistoryJpaEntity, Long> {
}
