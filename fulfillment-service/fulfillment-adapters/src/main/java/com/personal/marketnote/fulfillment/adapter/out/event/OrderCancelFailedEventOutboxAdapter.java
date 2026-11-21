package com.personal.marketnote.fulfillment.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderCancelFailedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.fulfillment.exception.OrderCancelFailedEventSerializationException;
import com.personal.marketnote.fulfillment.port.out.event.PublishOrderCancelFailedEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
@RequiredArgsConstructor
public class OrderCancelFailedEventOutboxAdapter implements PublishOrderCancelFailedEventPort {

    private static final String EVENT_SOURCE = "fulfillment-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publish(Long orderId, Long buyerId) {
        OrderCancelFailedEvent payload = new OrderCancelFailedEvent(orderId, buyerId);
        String topic = KafkaTopicConstants.ORDER_CANCEL_FAILED;
        String partitionKey = String.valueOf(orderId);
        EventEnvelope<OrderCancelFailedEvent> envelope = EventEnvelope.of(
                topic, EVENT_SOURCE, payload, clock
        );
        String serializedPayload = serialize(envelope, topic, partitionKey);
        OutboxEvent outboxEvent = OutboxEvent.of(
                envelope.eventId(),
                topic,
                partitionKey,
                OrderCancelFailedEvent.class.getSimpleName(),
                EVENT_SOURCE,
                serializedPayload,
                clock
        );
        saveOutboxEventPort.save(outboxEvent);
    }

    private String serialize(EventEnvelope<OrderCancelFailedEvent> envelope, String topic, String partitionKey) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new OrderCancelFailedEventSerializationException(topic, partitionKey, envelope.eventId(), e);
        }
    }
}
