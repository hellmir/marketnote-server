package com.personal.marketnote.notification.adapter.out.persistence.notification;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.adapter.out.mapper.NotificationJpaEntityToDomainMapper;
import com.personal.marketnote.notification.adapter.out.persistence.notification.repository.NotificationJpaRepository;
import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.domain.notification.SendStatus;
import com.personal.marketnote.notification.adapter.out.persistence.notification.entity.NotificationJpaEntity;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SaveNotificationPort;
import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@PersistenceAdapter
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements FindNotificationPort, SaveNotificationPort, UpdateNotificationPort {

    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    public Optional<Notification> findActiveById(Long id) {
        return notificationJpaRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .flatMap(NotificationJpaEntityToDomainMapper::mapToDomain);
    }

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

    @Override
    public long countUnreadByUserId(Long userId) {
        return notificationJpaRepository.countByUserIdAndStatusAndIsRead(userId, EntityStatus.ACTIVE, false);
    }

    @Override
    public List<Notification> findScheduledNotificationsDue(LocalDateTime now) {
        return notificationJpaRepository.findScheduledNotificationsDue(
                        SendStatus.SCHEDULED, now, EntityStatus.ACTIVE)
                .stream()
                .flatMap(entity -> NotificationJpaEntityToDomainMapper.mapToDomain(entity).stream())
                .toList();
    }

    @Override
    public List<Notification> saveAll(List<Notification> notifications) {
        List<NotificationJpaEntity> entities = notifications.stream()
                .map(NotificationJpaEntity::from)
                .toList();
        List<NotificationJpaEntity> savedEntities = notificationJpaRepository.saveAll(entities);
        return savedEntities.stream()
                .flatMap(entity -> NotificationJpaEntityToDomainMapper.mapToDomain(entity).stream())
                .toList();
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = NotificationJpaEntity.from(notification);
        NotificationJpaEntity saved = notificationJpaRepository.save(entity);
        return NotificationJpaEntityToDomainMapper.mapToDomain(saved).orElseThrow();
    }

    @Override
    public void update(Notification notification) {
        NotificationJpaEntity entity = notificationJpaRepository.findById(notification.getId())
                .orElseThrow(() -> new com.personal.marketnote.notification.domain.notification.NotificationNotFoundException(notification.getId()));
        entity.updateFrom(notification);
    }

    @Override
    public void updateAll(List<Notification> notifications) {
        List<Long> ids = notifications.stream().map(Notification::getId).toList();
        Map<Long, NotificationJpaEntity> entityMap = notificationJpaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(NotificationJpaEntity::getId, e -> e));
        for (Notification notification : notifications) {
            NotificationJpaEntity entity = entityMap.get(notification.getId());
            if (FormatValidator.hasNoValue(entity)) {
                continue;
            }
            entity.updateFrom(notification);
        }
    }
}
