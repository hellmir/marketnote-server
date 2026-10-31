package com.personal.marketnote.notification.port.out.sse;

import com.personal.marketnote.notification.port.out.sse.payload.SseEventPayload;

public interface PublishSseEventPort {

    void publish(Long userId, NotificationSseEventType eventType, SseEventPayload payload);
}
