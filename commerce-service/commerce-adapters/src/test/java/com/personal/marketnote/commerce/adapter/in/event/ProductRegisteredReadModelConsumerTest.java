package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.commerce.port.out.product.SaveProductReadModelPort;
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
@DisplayName("ProductRegisteredReadModelConsumer 테스트")
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
                LocalDateTime.of(2026, 3, 31, 10, 0),
                payload
        );
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 Read Model을 upsert한다")
    void handleProductRegisteredEvent_success_upsertsReadModel() {
        // given
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                1L, 100L, 10L, "테스트 상품", "1", "테스트 브랜드", 10000L, 8000L, 100L
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
    @DisplayName("envelope이 null이면 즉시 acknowledge한다")
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
                1L, 100L, 10L, "테스트 상품", "1", "테스트 브랜드", 10000L, 8000L, 100L
        );
        EventEnvelope<ProductRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                "wrong.event.type",
                "product-service",
                LocalDateTime.of(2026, 3, 31, 10, 0),
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
    @DisplayName("productId가 null이면 이벤트를 무시하고 acknowledge한다")
    void handleProductRegisteredEvent_nullProductId_acknowledges() {
        // given
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                null, 100L, 10L, "테스트 상품", "1", "테스트 브랜드", 10000L, 8000L, 100L
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
    @DisplayName("pricePolicyId가 null이면 이벤트를 무시하고 acknowledge한다")
    void handleProductRegisteredEvent_nullPricePolicyId_acknowledges() {
        // given
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                1L, null, 10L, "테스트 상품", "1", "테스트 브랜드", 10000L, 8000L, 100L
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
    @DisplayName("sellerId가 null이면 이벤트를 무시하고 acknowledge한다")
    void handleProductRegisteredEvent_nullSellerId_acknowledges() {
        // given
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                1L, 100L, null, "테스트 상품", "1", "테스트 브랜드", 10000L, 8000L, 100L
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
    @DisplayName("productId가 0이면 이벤트를 무시하고 acknowledge한다")
    void handleProductRegisteredEvent_zeroProductId_acknowledges() {
        // given
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                0L, 100L, 10L, "테스트 상품", "1", "테스트 브랜드", 10000L, 8000L, 100L
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
    @DisplayName("pricePolicyId가 음수이면 이벤트를 무시하고 acknowledge한다")
    void handleProductRegisteredEvent_negativePricePolicyId_acknowledges() {
        // given
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                1L, -1L, 10L, "테스트 상품", "1", "테스트 브랜드", 10000L, 8000L, 100L
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
    @DisplayName("nullable 필드가 null이어도 정상적으로 upsert한다")
    void handleProductRegisteredEvent_nullableFieldsNull_upsertsSuccessfully() {
        // given
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                1L, 100L, 10L, "테스트 상품", "1", null, null, null, null
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

    @Test
    @DisplayName("페이로드 역직렬화 실패 시 예외가 전파된다")
    void handleProductRegisteredEvent_deserializationFailure_propagatesException() {
        // given
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.PRODUCT_REGISTERED,
                "product-service",
                LocalDateTime.of(2026, 3, 31, 10, 0),
                "invalid-payload"
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.PRODUCT_REGISTERED, 0, 0, "1", envelope
        );

        // when & then
        assertThatThrownBy(() -> consumer.handleProductRegisteredEvent(record, acknowledgment))
                .isInstanceOf(IllegalArgumentException.class);

        verify(acknowledgment, never()).acknowledge();
    }
}
