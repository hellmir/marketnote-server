package com.personal.marketnote.notification.port.out.preference;

import com.personal.marketnote.notification.domain.preference.NotificationPreference;

import java.util.List;

public interface FindNotificationPreferencePort {
    List<NotificationPreference> findAllByUserId(Long userId);
}
