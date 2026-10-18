package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.InventoryAlreadyExistsException;
import com.personal.marketnote.commerce.port.in.command.inventory.BackfillInventoryProductKeyCommand;
import com.personal.marketnote.commerce.port.in.command.inventory.RegisterInventoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.BackfillInventoryProductKeyUseCase;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RegisterInventoryUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventKafkaListener {
    private final RegisterInventoryUseCase registerInventoryUseCase;
    private final BackfillInventoryProductKeyUseCase backfillInventoryProductKeyUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.PRODUCT_REGISTERED,
            groupId = "commerce-service"
    )
    public void handleProductRegisteredEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.PRODUCT_REGISTERED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            ProductRegisteredEvent payload = envelope.getPayloadAs(ProductRegisteredEvent.class, objectMapper);

            log.info("상품 등록 이벤트 수신. eventId={}, productId={}, productKey={}, pricePolicyId={}",
                    envelope.eventId(), payload.productId(), payload.productKey(), payload.pricePolicyId()
            );

            if (EventPayloadValidator.hasInvalidIds(
                    envelope.eventId(),
                    EventPayloadValidator.id("productId", payload.productId()),
                    EventPayloadValidator.id("pricePolicyId", payload.pricePolicyId())
            )) {
                acknowledgment.acknowledge();
                return;
            }

            if (FormatValidator.hasNoValue(payload.productKey())) {
                log.warn("상품 등록 이벤트 productKey 누락. eventId={}, productId={}",
                        envelope.eventId(), payload.productId()
                );
            }

            RegisterInventoryCommand command = RegisterInventoryCommand.of(
                    payload.productId(), payload.productKey(), payload.pricePolicyId()
            );
            registerInventoryUseCase.registerInventory(command);

            log.info("Kafka 이벤트로 재고 등록 완료. productId={}, pricePolicyId={}",
                    payload.productId(), payload.pricePolicyId()
            );
        } catch (InventoryAlreadyExistsException e) {
            log.info("재고가 이미 존재합니다 (멱등 처리). eventId={}, key={}", envelope.eventId(), record.key());
        }

        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = KafkaTopicConstants.PRODUCT_UPDATED,
            groupId = "commerce-inventory-backfill"
    )
    public void handleProductUpdatedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.PRODUCT_UPDATED)) {
            acknowledgment.acknowledge();
            return;
        }

        ProductUpdatedEvent payload = envelope.getPayloadAs(ProductUpdatedEvent.class, objectMapper);

        if (EventPayloadValidator.hasInvalidIds(
                envelope.eventId(),
                EventPayloadValidator.id("productId", payload.productId())
        )) {
            acknowledgment.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.productKey())) {
            log.warn("상품 수정 이벤트 productKey 누락. 백필 스킵. eventId={}, productId={}",
                    envelope.eventId(), payload.productId()
            );
            acknowledgment.acknowledge();
            return;
        }

        backfillInventoryProductKeyUseCase.backfillProductKey(
                BackfillInventoryProductKeyCommand.of(payload.productId(), payload.productKey())
        );

        acknowledgment.acknowledge();
    }
}
