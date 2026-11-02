package com.personal.marketnote.fulfillment.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.FulfillmentInventorySyncedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.fulfillment.exception.FulfillmentInventorySyncedEventSerializationException;
import com.personal.marketnote.fulfillment.port.out.event.PublishFulfillmentInventorySyncedEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FulfillmentInventorySyncedEventOutboxAdapter implements PublishFulfillmentInventorySyncedEventPort {

    private static final String EVENT_SOURCE = "fulfillment-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publish(FulfillmentInventorySyncedEvent event) {
        String payload = serialize(event);
        OutboxEvent outboxEvent = OutboxEvent.of(
                UUID.randomUUID().toString(),
                KafkaTopicConstants.FULFILLMENT_INVENTORY_SYNCED,
                "fulfillment-inventory-sync",
                FulfillmentInventorySyncedEvent.class.getSimpleName(),
                EVENT_SOURCE,
                payload,
                clock
        );
        saveOutboxEventPort.save(outboxEvent);
    }

    private String serialize(FulfillmentInventorySyncedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new FulfillmentInventorySyncedEventSerializationException(e);
        }
    }
}
