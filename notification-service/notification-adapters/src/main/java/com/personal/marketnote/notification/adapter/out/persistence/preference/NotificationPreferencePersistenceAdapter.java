package com.personal.marketnote.notification.adapter.out.persistence.preference;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.mapper.NotificationPreferenceJpaEntityToDomainMapper;
import com.personal.marketnote.notification.adapter.out.persistence.preference.entity.NotificationPreferenceJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.preference.repository.NotificationPreferenceJpaRepository;
import com.personal.marketnote.notification.domain.preference.DuplicateNotificationPreferenceException;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import com.personal.marketnote.notification.port.out.preference.SaveNotificationPreferencePort;
import com.personal.marketnote.notification.port.out.preference.UpdateNotificationPreferencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class NotificationPreferencePersistenceAdapter implements SaveNotificationPreferencePort, FindNotificationPreferencePort, UpdateNotificationPreferencePort {

    private final NotificationPreferenceJpaRepository notificationPreferenceJpaRepository;

    @Override
    public void saveAll(List<NotificationPreference> preferences) {
        try {
            List<NotificationPreferenceJpaEntity> entities = preferences.stream()
                    .map(NotificationPreferenceJpaEntity::from)
                    .toList();
            notificationPreferenceJpaRepository.saveAll(entities);
        } catch (DataIntegrityViolationException dive) {
            Long userId = preferences.isEmpty() ? null : preferences.get(0).getUserId();
            throw new DuplicateNotificationPreferenceException(userId);
        }
    }

    @Override
    public List<NotificationPreference> findAllByUserId(Long userId) {
        return notificationPreferenceJpaRepository.findAllByUserIdAndStatus(userId, EntityStatus.ACTIVE).stream()
                .map(NotificationPreferenceJpaEntityToDomainMapper::mapToDomain)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public Optional<NotificationPreference> findByUserIdAndNotificationType(Long userId, NotificationType notificationType) {
        return notificationPreferenceJpaRepository
                .findByUserIdAndNotificationTypeAndStatus(userId, notificationType, EntityStatus.ACTIVE)
                .flatMap(NotificationPreferenceJpaEntityToDomainMapper::mapToDomain);
    }

    @Override
    public void update(NotificationPreference preference) {
        notificationPreferenceJpaRepository.findById(preference.getId())
                .ifPresent(entity -> entity.updateFrom(preference));
    }
}
