package com.personal.marketnote.community.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.adapter.out.persistence.product.entity.ProductReadModelJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.product.repository.ProductReadModelJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductUpdatedReadModelConsumer {
    private final ObjectMapper objectMapper;
    private final ProductReadModelJpaRepository productReadModelJpaRepository;

    @KafkaListener(
            topics = KafkaTopicConstants.PRODUCT_UPDATED,
            groupId = "community-product-read-model"
    )
    @Transactional(isolation = READ_COMMITTED)
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

        log.info("상품 업데이트 이벤트 수신 (커뮤니티 Read Model). eventId={}, productId={}",
                envelope.eventId(), payload.productId());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("productId", payload.productId()))) {
            acknowledgment.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.productName())) {
            log.warn("상품 이름이 없어 Read Model 업데이트를 건너뜁니다. eventId={}, productId={}",
                    envelope.eventId(), payload.productId());
            acknowledgment.acknowledge();
            return;
        }

        List<ProductReadModelJpaEntity> entities =
                productReadModelJpaRepository.findByProductId(payload.productId());

        for (ProductReadModelJpaEntity entity : entities) {
            entity.updateFrom(
                    entity.getProductId(),
                    entity.getSellerId(),
                    payload.productName(),
                    entity.getBrandName(),
                    entity.getPrice(),
                    entity.getDiscountPrice(),
                    entity.getAccumulatedPoint()
            );
            log.info("상품 Read Model 이름 업데이트 완료. pricePolicyId={}, productId={}, newName={}",
                    entity.getPricePolicyId(), payload.productId(), payload.productName());
        }

        acknowledgment.acknowledge();
    }
}
