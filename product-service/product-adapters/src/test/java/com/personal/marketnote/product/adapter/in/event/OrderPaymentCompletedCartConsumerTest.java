package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent.OrderProductItem;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentCompletedCartConsumer 테스트")
class OrderPaymentCompletedCartConsumerTest {
    @InjectMocks
    private OrderPaymentCompletedCartConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, Long buyerId,
                                                                 Long totalAmount, Long pointAmount,
                                                                 List<OrderProductItem> orderProducts) {
        OrderPaymentCompletedEvent event = new OrderPaymentCompletedEvent(
                orderId, buyerId, totalAmount, pointAmount, orderProducts, null
        );
        EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.payment-completed", "commerce-service",
                LocalDateTime.of(2026, 3, 5, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.payment-completed", 0, 0L,
                String.valueOf(orderId), envelope);
    }

    private List<OrderProductItem> createOrderProductItems() {
        return List.of(
                new OrderProductItem(100L, 200L, 2, 30000L),
                new OrderProductItem(101L, null, 1, 20000L)
        );
    }

    @Test
    @DisplayName("주문 결제 완료 이벤트 수신 시 이벤트를 검증하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_success_validatesAndAcknowledges() {
        // given
        List<OrderProductItem> items = createOrderProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 50L, 80000L, 5000L, items);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 null이면 acknowledge하고 종료한다")
    void handleOrderPaymentCompletedEvent_nullBuyerId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> items = createOrderProductItems();
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null, 80000L, 5000L, items);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderProducts가 null이면 acknowledge하고 종료한다")
    void handleOrderPaymentCompletedEvent_nullOrderProducts_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 50L, 80000L, 5000L, null);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderProducts가 빈 리스트이면 acknowledge하고 종료한다")
    void handleOrderPaymentCompletedEvent_emptyOrderProducts_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 50L, 80000L, 5000L, List.of());
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleOrderPaymentCompletedEvent_unexpectedException_propagatesForRetry() {
        // given
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.payment-completed", "commerce-service",
                LocalDateTime.of(2026, 3, 5, 10, 0), "invalid-payload"
        );
        @SuppressWarnings("unchecked")
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.payment-completed", 0, 0L, "1",
                (EventEnvelope<?>) (EventEnvelope) envelope
        );
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderPaymentCompletedEvent(record, acknowledgment))
                .isInstanceOf(Exception.class);

        verify(acknowledgment, never()).acknowledge();
    }
}
