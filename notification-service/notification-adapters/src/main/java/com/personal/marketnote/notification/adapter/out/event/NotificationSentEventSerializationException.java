package com.personal.marketnote.notification.adapter.out.event;

public class NotificationSentEventSerializationException extends RuntimeException {

    public NotificationSentEventSerializationException(Long notificationId, Throwable cause) {
        super("알림 발송 완료 이벤트 직렬화 실패: notificationId=" + notificationId, cause);
    }
}
