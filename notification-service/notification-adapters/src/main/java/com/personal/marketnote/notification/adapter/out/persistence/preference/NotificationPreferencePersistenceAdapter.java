package com.personal.marketnote.notification.adapter.out.persistence.preference;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.notification.adapter.out.persistence.preference.entity.NotificationPreferenceJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.preference.repository.NotificationPreferenceJpaRepository;
import com.personal.marketnote.notification.domain.preference.DuplicateNotificationPreferenceException;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.port.out.preference.SaveNotificationPreferencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class NotificationPreferencePersistenceAdapter implements SaveNotificationPreferencePort {

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
}
