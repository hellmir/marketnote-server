package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.product.ProductReadModelPersistenceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
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
public class ProductUpdatedReadModelConsumer {

    private final ObjectMapper objectMapper;
    private final ProductReadModelPersistenceAdapter productReadModelPersistenceAdapter;

    @KafkaListener(
            topics = KafkaTopicConstants.PRODUCT_UPDATED,
            groupId = "commerce-product-read-model"
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

        log.info("상품 수정 이벤트 수신 (Read Model). eventId={}, productId={}",
                envelope.eventId(), payload.productId());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("productId", payload.productId()))) {
            acknowledgment.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.productName())) {
            log.warn("상품 수정 이벤트 productName이 null. eventId={}", envelope.eventId());
            acknowledgment.acknowledge();
            return;
        }

        productReadModelPersistenceAdapter.updateNameByProductId(payload.productId(), payload.productName());

        log.info("상품 Read Model 이름 업데이트 완료. productId={}, productName={}",
                payload.productId(), payload.productName());

        acknowledgment.acknowledge();
    }
}
