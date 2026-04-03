package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.product.ProductReadModelPersistenceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductUpdatedReadModelConsumer 테스트")
class ProductUpdatedReadModelConsumerTest {

    @InjectMocks
    private ProductUpdatedReadModelConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProductReadModelPersistenceAdapter productReadModelPersistenceAdapter;

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
                LocalDateTime.of(2026, 3, 31, 10, 0),
                payload
        );
    }

    private ProductUpdatedEvent createPayload(Long productId, String productName) {
        return new ProductUpdatedEvent(
                productId, productName, "1", "01",
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null
        );
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 Read Model 이름을 업데이트한다")
    void handleProductUpdatedEvent_success_updatesReadModelName() {
        // given
        ProductUpdatedEvent payload = createPayload(1L, "수정된 상품명");
        EventEnvelope<ProductUpdatedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verify(productReadModelPersistenceAdapter).updateNameByProductId(1L, "수정된 상품명");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 즉시 acknowledge한다")
    void handleProductUpdatedEvent_nullEnvelope_acknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.PRODUCT_UPDATED, 0, 0, "1", null
        );

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(productReadModelPersistenceAdapter);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 타입이 불일치하면 즉시 acknowledge한다")
    void handleProductUpdatedEvent_eventTypeMismatch_acknowledges() {
        // given
        ProductUpdatedEvent payload = createPayload(1L, "수정된 상품명");
        EventEnvelope<ProductUpdatedEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                "wrong.event.type",
                "product-service",
                LocalDateTime.of(2026, 3, 31, 10, 0),
                payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(productReadModelPersistenceAdapter);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 null이면 이벤트를 무시하고 acknowledge한다")
    void handleProductUpdatedEvent_nullProductId_acknowledges() {
        // given
        ProductUpdatedEvent payload = createPayload(null, "수정된 상품명");
        EventEnvelope<ProductUpdatedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(productReadModelPersistenceAdapter);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 0이면 이벤트를 무시하고 acknowledge한다")
    void handleProductUpdatedEvent_zeroProductId_acknowledges() {
        // given
        ProductUpdatedEvent payload = createPayload(0L, "수정된 상품명");
        EventEnvelope<ProductUpdatedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(productReadModelPersistenceAdapter);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 음수이면 이벤트를 무시하고 acknowledge한다")
    void handleProductUpdatedEvent_negativeProductId_acknowledges() {
        // given
        ProductUpdatedEvent payload = createPayload(-1L, "수정된 상품명");
        EventEnvelope<ProductUpdatedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(productReadModelPersistenceAdapter);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productName이 null이면 이벤트를 무시하고 acknowledge한다")
    void handleProductUpdatedEvent_nullProductName_acknowledges() {
        // given
        ProductUpdatedEvent payload = createPayload(1L, null);
        EventEnvelope<ProductUpdatedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(productReadModelPersistenceAdapter);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("페이로드 역직렬화 실패 시 예외가 전파된다")
    void handleProductUpdatedEvent_deserializationFailure_propagatesException() {
        // given
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.PRODUCT_UPDATED,
                "product-service",
                LocalDateTime.of(2026, 3, 31, 10, 0),
                "invalid-payload"
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.PRODUCT_UPDATED, 0, 0, "1", envelope
        );

        // when & then
        assertThatThrownBy(() -> consumer.handleProductUpdatedEvent(record, acknowledgment))
                .isInstanceOf(IllegalArgumentException.class);

        verify(acknowledgment, never()).acknowledge();
    }
}
