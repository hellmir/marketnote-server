package com.personal.marketnote.notification.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.PushNotificationSentEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.notification.port.out.event.PublishNotificationSentEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationSentEventOutboxAdapter implements PublishNotificationSentEventPort {

    private static final String EVENT_SOURCE = "notification-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publish(PushNotificationSentEvent event) {
        String payload = serialize(event);
        OutboxEvent outboxEvent = OutboxEvent.of(
                UUID.randomUUID().toString(),
                KafkaTopicConstants.NOTIFICATION_PUSH_SENT,
                String.valueOf(event.notificationId()),
                PushNotificationSentEvent.class.getSimpleName(),
                EVENT_SOURCE,
                payload,
                clock
        );
        saveOutboxEventPort.save(outboxEvent);
    }

    private String serialize(PushNotificationSentEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new NotificationSentEventSerializationException(event.notificationId(), e);
        }
    }
}
