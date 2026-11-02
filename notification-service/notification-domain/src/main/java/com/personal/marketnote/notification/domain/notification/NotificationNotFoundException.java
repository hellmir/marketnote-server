package com.personal.marketnote.notification.domain.notification;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(Long id) {
        super("ERR_NOTIFICATION_01::알림을 찾을 수 없습니다. id=" + id);
    }
}
