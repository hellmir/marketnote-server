package com.personal.marketnote.notification.port.out.sse;

public interface PublishSseEventPort {

    void publish(Long userId, NotificationSseEventType eventType, Object payload);
}
