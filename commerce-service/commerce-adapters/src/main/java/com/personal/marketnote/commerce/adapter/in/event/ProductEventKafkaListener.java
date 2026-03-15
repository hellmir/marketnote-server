package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.InventoryAlreadyExistsException;
import com.personal.marketnote.commerce.port.in.command.inventory.RegisterInventoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RegisterInventoryUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
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

            log.info("상품 등록 이벤트 수신. eventId={}, productId={}, pricePolicyId={}",
                    envelope.eventId(), payload.productId(), payload.pricePolicyId());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("productId", payload.productId()),
                    EventPayloadValidator.id("pricePolicyId", payload.pricePolicyId()))) {
                acknowledgment.acknowledge();
                return;
            }

            RegisterInventoryCommand command = RegisterInventoryCommand.of(
                    payload.productId(), payload.pricePolicyId()
            );
            registerInventoryUseCase.registerInventory(command);

            log.info("Kafka 이벤트로 재고 등록 완료. productId={}, pricePolicyId={}",
                    payload.productId(), payload.pricePolicyId());
        } catch (InventoryAlreadyExistsException e) {
            log.info("재고가 이미 존재합니다 (듀얼 라이트 중복). eventId={}, key={}",
                    envelope.eventId(), record.key());
        }

        acknowledgment.acknowledge();
    }
}
