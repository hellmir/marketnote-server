package com.personal.marketnote.notification.domain.preference;

public class DuplicateNotificationPreferenceException extends RuntimeException {
    public DuplicateNotificationPreferenceException(Long userId) {
        super("ERR_NOTIFICATION_PREFERENCE_02::이미 초기화된 알림 수신 설정입니다. userId=" + userId);
    }
}
