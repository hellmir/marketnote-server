package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.DuplicateLedgerTransactionException;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderCancelledEvent;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancelledLedgerConsumer 테스트")
class OrderCancelledLedgerConsumerTest {
    @InjectMocks
    private OrderCancelledLedgerConsumer consumer;

    @Mock
    private RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, Long cancelAmount) {
        OrderCancelledEvent event = new OrderCancelledEvent(
                orderId, "order-key-1", 50L, cancelAmount, 80000L, 1000L, 3000L,
                true, 0L, List.of(), List.of()
        );
        EventEnvelope<OrderCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.cancelled", "commerce-service",
                LocalDateTime.of(2026, 4, 8, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.cancelled", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("주문 취소 이벤트 수신 시 올바른 멱등성 키로 역분개를 기록하고 acknowledge한다")
    void handleOrderCancelledEvent_recordsPaymentCancellationWithIdempotencyKeyAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 60000L);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verify(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 60000L, "ORDER_CANCELLATION:1");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("다른 orderId로 이벤트 수신 시 해당 orderId 기반 멱등성 키가 생성된다")
    void handleOrderCancelledEvent_differentOrderId_generatesCorrectIdempotencyKey() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(999L, 45000L);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verify(recordLedgerEntryUseCase).recordPaymentCancellation(999L, 45000L, "ORDER_CANCELLATION:999");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 처리된 역분개에 대해 DuplicateLedgerTransactionException 발생 시 멱등 처리로 정상 acknowledge한다")
    void handleOrderCancelledEvent_duplicateTransaction_acknowledgesGracefully() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 60000L);
        doThrow(new DuplicateLedgerTransactionException("ORDER_CANCELLATION:1"))
                .when(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 60000L, "ORDER_CANCELLATION:1");

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verify(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 60000L, "ORDER_CANCELLATION:1");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.cancelled", 0, 0L, "1", null
        );

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(recordLedgerEntryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        OrderCancelledEvent event = new OrderCancelledEvent(
                1L, "order-key-1", 50L, 60000L, 80000L, 1000L, 3000L,
                true, 0L, List.of(), List.of()
        );
        EventEnvelope<OrderCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 4, 8, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.cancelled", 0, 0L, "1", envelope
        );

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(recordLedgerEntryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 60000L);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(recordLedgerEntryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, 60000L);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(recordLedgerEntryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, 60000L);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(recordLedgerEntryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleOrderCancelledEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 60000L);
        doThrow(new RuntimeException("DB 연결 오류"))
                .when(recordLedgerEntryUseCase).recordPaymentCancellation(anyLong(), anyLong(), anyString());

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderCancelledEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 오류");

        verify(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 60000L, "ORDER_CANCELLATION:1");
        verify(acknowledgment, never()).acknowledge();
    }
}
