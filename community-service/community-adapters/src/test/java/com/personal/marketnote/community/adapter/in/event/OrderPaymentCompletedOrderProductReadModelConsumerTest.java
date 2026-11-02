package com.personal.marketnote.community.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent.OrderProductItem;
import com.personal.marketnote.community.port.out.order.SaveOrderProductReadModelPort;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrderPaymentCompletedOrderProductReadModelConsumerTest {

    @InjectMocks
    private OrderPaymentCompletedOrderProductReadModelConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SaveOrderProductReadModelPort saveOrderProductReadModelPort;

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(EventEnvelope<?> envelope) {
        return new ConsumerRecord<>(
                KafkaTopicConstants.ORDER_PAYMENT_COMPLETED, 0, 0, "1", envelope
        );
    }

    private EventEnvelope<OrderPaymentCompletedEvent> createEnvelope(OrderPaymentCompletedEvent payload) {
        return new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
                "commerce-service",
                LocalDateTime.parse("2026-10-17T10:00:00"),
                payload
        );
    }

    private OrderPaymentCompletedEvent buildEvent(Long orderId, Long buyerId, List<OrderProductItem> items) {
        return new OrderPaymentCompletedEvent(
                orderId, buyerId, 10000L, 0L,
                "홍길동", "01012345678", "12345", "서울시 강남구", "역삼동",
                "요청 사항", items, 100L
        );
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 주문 상품별 ReadModel을 upsert한다")
    void handleEvent_validEvent_upsertsReadModelPerOrderProduct() {
        // given
        OrderProductItem item1 = new OrderProductItem(100L, 1L, null, UUID.randomUUID(), 1, 10000L);
        OrderProductItem item2 = new OrderProductItem(200L, 2L, null, UUID.randomUUID(), 2, 20000L);
        OrderPaymentCompletedEvent payload = buildEvent(10L, 1L, List.of(item1, item2));
        EventEnvelope<OrderPaymentCompletedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(saveOrderProductReadModelPort).upsert(10L, 100L, 1L, 10000L);
        verify(saveOrderProductReadModelPort).upsert(10L, 200L, 1L, 20000L);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope가 null이면 즉시 acknowledge한다")
    void handleEvent_nullEnvelope_acknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.ORDER_PAYMENT_COMPLETED, 0, 0, "1", null
        );

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveOrderProductReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 타입이 불일치하면 즉시 acknowledge한다")
    void handleEvent_eventTypeMismatch_acknowledges() {
        // given
        OrderProductItem item = new OrderProductItem(100L, 1L, null, UUID.randomUUID(), 1, 10000L);
        OrderPaymentCompletedEvent payload = buildEvent(10L, 1L, List.of(item));
        EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.parse("2026-10-17T10:00:00"), payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveOrderProductReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 유효하지 않으면 즉시 acknowledge한다")
    void handleEvent_invalidOrderId_acknowledges() {
        // given
        OrderProductItem item = new OrderProductItem(100L, 1L, null, UUID.randomUUID(), 1, 10000L);
        OrderPaymentCompletedEvent payload = buildEvent(0L, 1L, List.of(item));
        EventEnvelope<OrderPaymentCompletedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveOrderProductReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 유효하지 않으면 즉시 acknowledge한다")
    void handleEvent_invalidBuyerId_acknowledges() {
        // given
        OrderProductItem item = new OrderProductItem(100L, 1L, null, UUID.randomUUID(), 1, 10000L);
        OrderPaymentCompletedEvent payload = buildEvent(10L, -1L, List.of(item));
        EventEnvelope<OrderPaymentCompletedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveOrderProductReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderProducts가 null이면 upsert 없이 acknowledge한다")
    void handleEvent_nullOrderProducts_acknowledges() {
        // given
        OrderPaymentCompletedEvent payload = buildEvent(10L, 1L, null);
        EventEnvelope<OrderPaymentCompletedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveOrderProductReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderProducts가 빈 리스트면 upsert 없이 acknowledge한다")
    void handleEvent_emptyOrderProducts_acknowledges() {
        // given
        OrderPaymentCompletedEvent payload = buildEvent(10L, 1L, List.of());
        EventEnvelope<OrderPaymentCompletedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveOrderProductReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderProducts 중 pricePolicyId가 유효하지 않은 항목은 스킵하고 나머지는 upsert한다")
    void handleEvent_invalidPricePolicyIdItem_skipsAndContinues() {
        // given
        OrderProductItem invalid = new OrderProductItem(null, 1L, null, UUID.randomUUID(), 1, 10000L);
        OrderProductItem valid = new OrderProductItem(200L, 2L, null, UUID.randomUUID(), 2, 20000L);
        OrderPaymentCompletedEvent payload = buildEvent(10L, 1L, List.of(invalid, valid));
        EventEnvelope<OrderPaymentCompletedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(saveOrderProductReadModelPort).upsert(10L, 200L, 1L, 20000L);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderProducts 중 unitAmount가 유효하지 않은 항목은 스킵한다")
    void handleEvent_invalidUnitAmountItem_skips() {
        // given
        OrderProductItem item = new OrderProductItem(100L, 1L, null, UUID.randomUUID(), 1, null);
        OrderPaymentCompletedEvent payload = buildEvent(10L, 1L, List.of(item));
        EventEnvelope<OrderPaymentCompletedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveOrderProductReadModelPort);
        verify(acknowledgment).acknowledge();
    }
}
