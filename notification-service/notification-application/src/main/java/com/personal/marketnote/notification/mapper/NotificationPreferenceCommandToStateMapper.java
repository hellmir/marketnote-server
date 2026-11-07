package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.notification.domain.preference.NotificationPreferenceCreateState;
import com.personal.marketnote.notification.domain.template.NotificationType;

public class NotificationPreferenceCommandToStateMapper {

    private NotificationPreferenceCommandToStateMapper() {
    }

    public static NotificationPreferenceCreateState mapToState(Long userId, NotificationType type) {
        return NotificationPreferenceCreateState.builder()
                .userId(userId)
                .notificationType(type)
                .enabled(type.isDefaultEnabled())
                .build();
    }
}
