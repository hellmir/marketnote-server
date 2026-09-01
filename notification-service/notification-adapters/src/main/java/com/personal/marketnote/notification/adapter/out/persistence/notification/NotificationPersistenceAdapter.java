package com.personal.marketnote.notification.adapter.out.persistence.notification;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.adapter.out.mapper.NotificationJpaEntityToDomainMapper;
import com.personal.marketnote.notification.adapter.out.persistence.notification.repository.NotificationJpaRepository;
import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements FindNotificationPort {

    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    public List<Notification> findByUserId(Long userId, Long cursor, int fetchSize) {
        Long effectiveCursor = FormatValidator.hasValue(cursor) ? cursor : -1L;

        return notificationJpaRepository.findByUserIdWithCursor(
                        userId, EntityStatus.ACTIVE, effectiveCursor, PageRequest.of(0, fetchSize)
                ).stream()
                .flatMap(entity -> NotificationJpaEntityToDomainMapper.mapToDomain(entity).stream())
                .toList();
    }

    @Override
    public long countByUserId(Long userId) {
        return notificationJpaRepository.countByUserIdAndStatus(userId, EntityStatus.ACTIVE);
    }
}
