package com.personal.marketnote.product.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PricePolicyCreatedEvent;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.product.port.out.event.PublishProductEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class ProductEventKafkaProducer implements PublishProductEventPort {
    private static final String SOURCE = "product-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publishProductRegisteredEvent(Long productId, Long pricePolicyId, Long sellerId, String productName, String goodsType, String brandName, Long price, Long discountPrice, Long accumulatedPoint) {
        ProductRegisteredEvent payload = new ProductRegisteredEvent(productId, pricePolicyId, sellerId, productName, goodsType, brandName, price, discountPrice, accumulatedPoint);
        String topic = KafkaTopicConstants.PRODUCT_REGISTERED;
        EventEnvelope<ProductRegisteredEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

        saveToOutbox(envelope, topic, productId.toString());
    }

    @Override
    public void publishPricePolicyCreatedEvent(Long productId, Long pricePolicyId) {
        PricePolicyCreatedEvent payload = new PricePolicyCreatedEvent(productId, pricePolicyId);
        String topic = KafkaTopicConstants.PRICE_POLICY_CREATED;
        EventEnvelope<PricePolicyCreatedEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

        saveToOutbox(envelope, topic, productId.toString());
    }

    @Override
    public void publishProductUpdatedEvent(ProductUpdatedEvent payload) {
        String topic = KafkaTopicConstants.PRODUCT_UPDATED;
        EventEnvelope<ProductUpdatedEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

        saveToOutbox(envelope, topic, payload.productId().toString());
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
