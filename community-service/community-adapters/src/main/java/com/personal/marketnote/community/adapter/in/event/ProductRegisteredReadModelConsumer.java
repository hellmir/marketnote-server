package com.personal.marketnote.community.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.community.port.out.product.SaveProductReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRegisteredReadModelConsumer {
    private final ObjectMapper objectMapper;
    private final SaveProductReadModelPort saveProductReadModelPort;

    @KafkaListener(
            topics = KafkaTopicConstants.PRODUCT_REGISTERED,
            groupId = "community-product-read-model"
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

        ProductRegisteredEvent payload = envelope.getPayloadAs(ProductRegisteredEvent.class, objectMapper);

        log.info("상품 등록 이벤트 수신 (커뮤니티 Read Model). eventId={}, productId={}, pricePolicyId={}",
                envelope.eventId(), payload.productId(), payload.pricePolicyId());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("productId", payload.productId()),
                EventPayloadValidator.id("pricePolicyId", payload.pricePolicyId()))) {
            acknowledgment.acknowledge();
            return;
        }

        saveProductReadModelPort.upsert(
                payload.pricePolicyId(),
                payload.productId(),
                payload.sellerId(),
                payload.productName(),
                payload.brandName(),
                payload.price(),
                payload.discountPrice(),
                payload.accumulatedPoint()
        );

        log.info("상품 Read Model 저장 완료. pricePolicyId={}, productId={}",
                payload.pricePolicyId(), payload.productId());

        acknowledgment.acknowledge();
    }
}
