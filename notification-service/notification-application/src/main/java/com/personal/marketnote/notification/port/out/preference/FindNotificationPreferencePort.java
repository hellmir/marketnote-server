package com.personal.marketnote.notification.port.out.preference;

import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.template.NotificationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FindNotificationPreferencePort {
    List<NotificationPreference> findAllByUserId(Long userId);

    Optional<NotificationPreference> findByUserIdAndNotificationType(Long userId, NotificationType notificationType);

    List<NotificationPreference> findEnabledByUserIdsAndNotificationType(List<Long> userIds, NotificationType notificationType);

    List<Long> findAllDistinctUserIds();

    List<NotificationPreference> findConsentReminderDue(LocalDateTime threshold);
}
