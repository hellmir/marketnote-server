package com.personal.marketnote.notification.port.out.event;

import com.personal.marketnote.common.kafka.event.PushNotificationSentEvent;

public interface PublishNotificationSentEventPort {

    void publish(PushNotificationSentEvent event);
}
