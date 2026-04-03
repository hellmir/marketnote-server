package com.personal.marketnote.community.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import com.personal.marketnote.community.adapter.out.persistence.product.entity.ProductReadModelJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.product.repository.ProductReadModelJpaRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductUpdatedReadModelConsumerTest {

    @InjectMocks
    private ProductUpdatedReadModelConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProductReadModelJpaRepository productReadModelJpaRepository;

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(EventEnvelope<?> envelope) {
        return new ConsumerRecord<>(
                KafkaTopicConstants.PRODUCT_UPDATED, 0, 0, "1", envelope
        );
    }

    private EventEnvelope<ProductUpdatedEvent> createEnvelope(ProductUpdatedEvent payload) {
        return new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.PRODUCT_UPDATED,
                "product-service",
                LocalDateTime.now(Clock.fixed(Instant.parse("2026-03-31T10:00:00Z"), ZoneId.of("Asia/Seoul"))),
                payload
        );
    }

    private ProductUpdatedEvent createPayload(Long productId, String productName) {
        return new ProductUpdatedEvent(
                productId, productName, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null
        );
    }

    private ProductReadModelJpaEntity createEntity(Long pricePolicyId, Long productId, String name) {
        ProductReadModelJpaEntity entity = ProductReadModelJpaEntity.of(
                pricePolicyId, productId, 10L, name, "브랜드", 10000L, 8000L, 100L
        );
        ReflectionTestUtils.setField(entity, "id", pricePolicyId);
        return entity;
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 해당 productId의 Read Model 이름을 업데이트한다")
    void handleProductUpdatedEvent_validEvent_updatesReadModel() {
        // given
        ProductUpdatedEvent payload = createPayload(1L, "변경된 상품명");
        EventEnvelope<ProductUpdatedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        ProductReadModelJpaEntity entity = createEntity(100L, 1L, "원래 상품명");
        when(productReadModelJpaRepository.findByProductId(1L)).thenReturn(List.of(entity));

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verify(productReadModelJpaRepository).findByProductId(1L);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope가 null이면 즉시 acknowledge한다")
    void handleProductUpdatedEvent_nullEnvelope_acknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.PRODUCT_UPDATED, 0, 0, "1", null
        );

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(productReadModelJpaRepository);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 타입이 불일치하면 즉시 acknowledge한다")
    void handleProductUpdatedEvent_eventTypeMismatch_acknowledges() {
        // given
        ProductUpdatedEvent payload = createPayload(1L, "상품명");
        EventEnvelope<ProductUpdatedEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                "wrong.event.type",
                "product-service",
                LocalDateTime.now(),
                payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(productReadModelJpaRepository);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 유효하지 않으면 즉시 acknowledge한다")
    void handleProductUpdatedEvent_invalidProductId_acknowledges() {
        // given
        ProductUpdatedEvent payload = createPayload(-1L, "상품명");
        EventEnvelope<ProductUpdatedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(productReadModelJpaRepository);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productName이 null이면 업데이트를 건너뛰고 acknowledge한다")
    void handleProductUpdatedEvent_nullProductName_skipsAndAcknowledges() {
        // given
        ProductUpdatedEvent payload = createPayload(1L, null);
        EventEnvelope<ProductUpdatedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(productReadModelJpaRepository);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("해당 productId의 Read Model이 없으면 업데이트 없이 acknowledge한다")
    void handleProductUpdatedEvent_noReadModel_acknowledges() {
        // given
        ProductUpdatedEvent payload = createPayload(999L, "상품명");
        EventEnvelope<ProductUpdatedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        when(productReadModelJpaRepository.findByProductId(999L)).thenReturn(List.of());

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verify(productReadModelJpaRepository).findByProductId(999L);
        verify(acknowledgment).acknowledge();
    }
}
