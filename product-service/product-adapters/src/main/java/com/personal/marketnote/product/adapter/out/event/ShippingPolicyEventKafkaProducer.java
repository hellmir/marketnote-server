package com.personal.marketnote.product.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ShippingPolicyChangeAction;
import com.personal.marketnote.common.kafka.event.ShippingPolicyChangedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.product.port.out.event.PublishShippingPolicyEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class ShippingPolicyEventKafkaProducer implements PublishShippingPolicyEventPort {
    private static final String SOURCE = "product-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publishShippingPolicyChangedEvent(Long sellerId, Long shippingFee, Long freeShippingThreshold,
                                                  Long jejuSurcharge, Long islandSurcharge, ShippingPolicyChangeAction action) {
        ShippingPolicyChangedEvent payload = new ShippingPolicyChangedEvent(
                sellerId, shippingFee, freeShippingThreshold, jejuSurcharge, islandSurcharge, action);
        String topic = KafkaTopicConstants.SHIPPING_POLICY_CHANGED;
        EventEnvelope<ShippingPolicyChangedEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

        saveToOutbox(envelope, topic, sellerId.toString());
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
