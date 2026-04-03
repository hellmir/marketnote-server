package com.personal.marketnote.commerce.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.out.event.PublishInventoryEventPort;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.InventoryChangeAction;
import com.personal.marketnote.common.kafka.event.InventoryChangedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class InventoryEventKafkaProducer implements PublishInventoryEventPort {
    private static final String SOURCE = "commerce-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publishInventoryChangedEvent(Long pricePolicyId, Long productId, Integer stockQuantity,
                                             InventoryChangeAction action) {
        InventoryChangedEvent payload = new InventoryChangedEvent(
                pricePolicyId, productId, stockQuantity, action
        );
        String topic = KafkaTopicConstants.INVENTORY_CHANGED;
        EventEnvelope<InventoryChangedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, pricePolicyId.toString());
    }

    private <T> void saveToOutbox(EventEnvelope<T> envelope, String topic, String partitionKey) {
        try {
            String payloadJson = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = OutboxEvent.of(
                    envelope.eventId(), topic, partitionKey,
                    envelope.eventType(), SOURCE, payloadJson, clock
            );
            saveOutboxEventPort.save(outboxEvent);
            log.info("Outbox 이벤트 저장. topic={}, partitionKey={}, eventId={}",
                    topic, partitionKey, envelope.eventId());
        } catch (Exception e) {
            log.error("Outbox 이벤트 저장 실패. topic={}, partitionKey={}, error={}",
                    topic, partitionKey, e.getMessage(), e);
        }
    }
}
