package com.personal.marketnote.notification.domain.preference;

import com.personal.marketnote.notification.domain.template.NotificationType;

public class NotificationPreferenceNotFoundException extends RuntimeException {
    public NotificationPreferenceNotFoundException(Long userId, NotificationType notificationType) {
        super("ERR_NOTIFICATION_PREFERENCE_03::알림 수신 설정을 찾을 수 없습니다. userId=" + userId + ", notificationType=" + notificationType);
    }
}
