package com.personal.marketnote.fulfillment.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.FulfillmentGoodsSyncedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.fulfillment.exception.FulfillmentGoodsSyncedEventSerializationException;
import com.personal.marketnote.fulfillment.port.out.event.PublishFulfillmentGoodsSyncedEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FulfillmentGoodsSyncedEventOutboxAdapter implements PublishFulfillmentGoodsSyncedEventPort {

    private static final String EVENT_SOURCE = "fulfillment-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publish(FulfillmentGoodsSyncedEvent event) {
        String topic = KafkaTopicConstants.FULFILLMENT_GOODS_SYNCED;
        String partitionKey = event.customerGoodsCode();
        String eventId = UUID.randomUUID().toString();
        String payload = serialize(event, topic, partitionKey, eventId);
        OutboxEvent outboxEvent = OutboxEvent.of(
                eventId,
                topic,
                partitionKey,
                FulfillmentGoodsSyncedEvent.class.getSimpleName(),
                EVENT_SOURCE,
                payload,
                clock
        );
        saveOutboxEventPort.save(outboxEvent);
    }

    private String serialize(FulfillmentGoodsSyncedEvent event, String topic, String partitionKey, String eventId) {
        EventEnvelope<FulfillmentGoodsSyncedEvent> envelope = EventEnvelope.of(
                topic,
                EVENT_SOURCE,
                event,
                clock
        );
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new FulfillmentGoodsSyncedEventSerializationException(topic, partitionKey, eventId, e);
        }
    }
}
