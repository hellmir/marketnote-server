package com.personal.marketnote.fulfillment.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.ShippingStatusChangedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.fulfillment.exception.ShippingStatusEventSerializationException;
import com.personal.marketnote.fulfillment.port.out.event.PublishShippingStatusChangedEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ShippingStatusEventOutboxAdapter implements PublishShippingStatusChangedEventPort {

    private static final String EVENT_SOURCE = "fulfillment-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publish(ShippingStatusChangedEvent event) {
        String topic = KafkaTopicConstants.SHIPPING_STATUS_CHANGED;
        String partitionKey = String.valueOf(event.orderId());
        String eventId = UUID.randomUUID().toString();
        String payload = serialize(event, topic, partitionKey, eventId);
        OutboxEvent outboxEvent = OutboxEvent.of(
                eventId,
                topic,
                partitionKey,
                ShippingStatusChangedEvent.class.getSimpleName(),
                EVENT_SOURCE,
                payload,
                clock
        );
        saveOutboxEventPort.save(outboxEvent);
    }

    private String serialize(ShippingStatusChangedEvent event, String topic, String partitionKey, String eventId) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new ShippingStatusEventSerializationException(topic, partitionKey, eventId, e);
        }
    }
}
