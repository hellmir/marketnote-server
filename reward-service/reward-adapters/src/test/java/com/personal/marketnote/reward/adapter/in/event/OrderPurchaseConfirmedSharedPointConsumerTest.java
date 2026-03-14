package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPurchaseConfirmedEvent;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPurchaseConfirmedSharedPointConsumer 테스트")
class OrderPurchaseConfirmedSharedPointConsumerTest {
    @InjectMocks
    private OrderPurchaseConfirmedSharedPointConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private final Acknowledgment acknowledgment = mock(Acknowledgment.class);

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, Long buyerId, List<Long> sharerIds) {
        OrderPurchaseConfirmedEvent event = new OrderPurchaseConfirmedEvent(orderId, buyerId, sharerIds);
        EventEnvelope<OrderPurchaseConfirmedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.purchase-confirmed", "commerce-service",
                LocalDateTime.of(2026, 3, 7, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.purchase-confirmed", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("구매 확정 이벤트 수신 시 공유 적립 예정 포인트 확정 검증 후 acknowledge한다")
    void handleOrderPurchaseConfirmedEvent_withSharers_validatesAndAcknowledges() {
        // given
        List<Long> sharerIds = List.of(200L, 300L);
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, sharerIds);

        // when
        consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 공유 적립 예정 포인트 확정 없이 acknowledge한다")
    void handleOrderPurchaseConfirmedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        List<Long> sharerIds = List.of(200L, 300L);
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 100L, sharerIds);

        // when
        consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("sharerIds가 null이면 공유 적립 예정 포인트 확정 없이 acknowledge한다")
    void handleOrderPurchaseConfirmedEvent_nullSharerIds_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, null);

        // when
        consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("sharerIds가 빈 목록이면 공유 적립 예정 포인트 확정 없이 acknowledge한다")
    void handleOrderPurchaseConfirmedEvent_emptySharerIds_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, List.of());

        // when
        consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 공유 적립 예정 포인트 확정 없이 acknowledge한다")
    void handleOrderPurchaseConfirmedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.purchase-confirmed", 0, 0L, "1", null
        );

        // when
        consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    @SuppressWarnings("unchecked")
    void handleOrderPurchaseConfirmedEvent_unexpectedException_propagatesForRetry() {
        // given
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.purchase-confirmed", "commerce-service",
                LocalDateTime.of(2026, 3, 7, 10, 0), "invalid-payload"
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.purchase-confirmed", 0, 0L, "1",
                (EventEnvelope<?>) (EventEnvelope) envelope
        );

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment))
                .isInstanceOf(Exception.class);

        verify(acknowledgment, never()).acknowledge();
    }
}
