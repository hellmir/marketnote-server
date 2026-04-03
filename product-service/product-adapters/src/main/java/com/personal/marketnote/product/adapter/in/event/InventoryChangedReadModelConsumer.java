package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.InventoryChangedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.port.out.inventory.SaveInventoryReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryChangedReadModelConsumer {

    private final ObjectMapper objectMapper;
    private final SaveInventoryReadModelPort saveInventoryReadModelPort;

    @KafkaListener(
            topics = KafkaTopicConstants.INVENTORY_CHANGED,
            groupId = "product-inventory-read-model"
    )
    public void handleInventoryChangedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.INVENTORY_CHANGED)) {
            acknowledgment.acknowledge();
            return;
        }

        InventoryChangedEvent payload = envelope.getPayloadAs(InventoryChangedEvent.class, objectMapper);

        log.info("재고 변경 이벤트 수신. eventId={}, pricePolicyId={}, productId={}, stockQuantity={}, action={}",
                envelope.eventId(), payload.pricePolicyId(), payload.productId(),
                payload.stockQuantity(), payload.action());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("pricePolicyId", payload.pricePolicyId()),
                EventPayloadValidator.id("productId", payload.productId()))) {
            acknowledgment.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.action())) {
            log.warn("재고 변경 이벤트 action이 null. eventId={}", envelope.eventId());
            acknowledgment.acknowledge();
            return;
        }

        if (payload.action().isCreated() || payload.action().isUpdated()) {
            saveInventoryReadModelPort.upsert(
                    payload.pricePolicyId(), payload.productId(), payload.stockQuantity()
            );
            log.info("재고 Read Model 저장 완료. pricePolicyId={}, productId={}",
                    payload.pricePolicyId(), payload.productId());
        }

        acknowledgment.acknowledge();
    }
}
