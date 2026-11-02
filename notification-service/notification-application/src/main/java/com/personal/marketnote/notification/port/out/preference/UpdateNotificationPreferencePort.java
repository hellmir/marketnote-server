package com.personal.marketnote.notification.port.out.preference;

import com.personal.marketnote.notification.domain.preference.NotificationPreference;

public interface UpdateNotificationPreferencePort {
    void update(NotificationPreference preference);
}
