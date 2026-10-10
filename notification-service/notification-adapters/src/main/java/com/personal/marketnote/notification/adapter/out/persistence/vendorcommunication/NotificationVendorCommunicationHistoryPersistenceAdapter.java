package com.personal.marketnote.notification.adapter.out.persistence.vendorcommunication;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.notification.adapter.out.persistence.vendorcommunication.entity.NotificationVendorCommunicationHistoryJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.vendorcommunication.repository.NotificationVendorCommunicationHistoryJpaRepository;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationHistory;
import com.personal.marketnote.notification.port.out.vendorcommunication.SaveNotificationVendorCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class NotificationVendorCommunicationHistoryPersistenceAdapter
        implements SaveNotificationVendorCommunicationHistoryPort {

    private final NotificationVendorCommunicationHistoryJpaRepository repository;

    @Override
    public NotificationVendorCommunicationHistory save(NotificationVendorCommunicationHistory history) {
        NotificationVendorCommunicationHistoryJpaEntity saved = repository.save(
                NotificationVendorCommunicationHistoryJpaEntity.from(history)
        );
        return saved.toDomain();
    }
}
