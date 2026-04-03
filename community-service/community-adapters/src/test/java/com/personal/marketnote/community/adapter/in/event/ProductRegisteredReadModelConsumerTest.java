package com.personal.marketnote.community.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.community.port.out.product.SaveProductReadModelPort;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRegisteredReadModelConsumerTest {

    @InjectMocks
    private ProductRegisteredReadModelConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SaveProductReadModelPort saveProductReadModelPort;

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(EventEnvelope<?> envelope) {
        return new ConsumerRecord<>(
                KafkaTopicConstants.PRODUCT_REGISTERED, 0, 0, "1", envelope
        );
    }

    private EventEnvelope<ProductRegisteredEvent> createEnvelope(ProductRegisteredEvent payload) {
        return new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.PRODUCT_REGISTERED,
                "product-service",
                LocalDateTime.now(Clock.fixed(Instant.parse("2026-03-31T10:00:00Z"), ZoneId.of("Asia/Seoul"))),
                payload
        );
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 Read Model을 upsert한다")
    void handleProductRegisteredEvent_validEvent_upsertsReadModel() {
        // given
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                1L, 100L, 10L, "테스트 상품", "1",
                "테스트 브랜드", 10000L, 8000L, 100L
        );
        EventEnvelope<ProductRegisteredEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verify(saveProductReadModelPort).upsert(
                100L, 1L, 10L, "테스트 상품", "테스트 브랜드", 10000L, 8000L, 100L
        );
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope가 null이면 즉시 acknowledge한다")
    void handleProductRegisteredEvent_nullEnvelope_acknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.PRODUCT_REGISTERED, 0, 0, "1", null
        );

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveProductReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 타입이 불일치하면 즉시 acknowledge한다")
    void handleProductRegisteredEvent_eventTypeMismatch_acknowledges() {
        // given
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                1L, 100L, 10L, "테스트 상품", "1",
                "테스트 브랜드", 10000L, 8000L, 100L
        );
        EventEnvelope<ProductRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                "wrong.event.type",
                "product-service",
                LocalDateTime.now(),
                payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveProductReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 유효하지 않으면 즉시 acknowledge한다")
    void handleProductRegisteredEvent_invalidProductId_acknowledges() {
        // given
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                -1L, 100L, 10L, "테스트 상품", "1",
                "테스트 브랜드", 10000L, 8000L, 100L
        );
        EventEnvelope<ProductRegisteredEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveProductReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("pricePolicyId가 유효하지 않으면 즉시 acknowledge한다")
    void handleProductRegisteredEvent_invalidPricePolicyId_acknowledges() {
        // given
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                1L, 0L, 10L, "테스트 상품", "1",
                "테스트 브랜드", 10000L, 8000L, 100L
        );
        EventEnvelope<ProductRegisteredEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveProductReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("nullable 필드(brandName, price 등)가 null이어도 정상 upsert한다")
    void handleProductRegisteredEvent_nullableFields_upsertsReadModel() {
        // given
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                1L, 100L, 10L, "테스트 상품", "1",
                null, null, null, null
        );
        EventEnvelope<ProductRegisteredEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verify(saveProductReadModelPort).upsert(
                100L, 1L, 10L, "테스트 상품", null, null, null, null
        );
        verify(acknowledgment).acknowledge();
    }
}
