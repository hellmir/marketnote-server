package com.personal.marketnote.user.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ShippingAddressChangeAction;
import com.personal.marketnote.common.kafka.event.ShippingAddressChangedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.user.port.out.event.PublishShippingAddressEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class ShippingAddressEventKafkaProducer implements PublishShippingAddressEventPort {
    private static final String SOURCE = "user-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publishShippingAddressChangedEvent(Long shippingAddressId, Long userId, String recipientName, String recipientPhoneNumber, String address, String addressDetail, String regionType, ShippingAddressChangeAction action) {
        ShippingAddressChangedEvent payload = new ShippingAddressChangedEvent(shippingAddressId, userId, recipientName, recipientPhoneNumber, address, addressDetail, regionType, action);
        String topic = KafkaTopicConstants.SHIPPING_ADDRESS_CHANGED;
        EventEnvelope<ShippingAddressChangedEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

        saveToOutbox(envelope, topic, userId.toString());
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
