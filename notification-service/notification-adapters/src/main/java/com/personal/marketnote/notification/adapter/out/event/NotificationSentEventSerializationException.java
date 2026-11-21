package com.personal.marketnote.notification.adapter.out.event;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class NotificationSentEventSerializationException extends BaseEventSerializationException {

    public NotificationSentEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
        super("ERR_NOTIFICATION_SENT_EVENT_01", "알림 발송 완료 이벤트", topic, partitionKey, eventId, cause);
    }
}
