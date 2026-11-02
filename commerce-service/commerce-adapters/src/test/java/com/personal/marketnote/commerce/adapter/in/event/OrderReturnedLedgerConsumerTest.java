package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.DuplicateLedgerTransactionException;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderReturnedEvent;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderReturnedLedgerConsumer 테스트")
class OrderReturnedLedgerConsumerTest {
    @InjectMocks
    private OrderReturnedLedgerConsumer consumer;

    @Mock
    private RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, Long returnAmount) {
        return buildRecord(orderId, returnAmount, null);
    }

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, Long returnAmount, String returnId) {
        OrderReturnedEvent event = new OrderReturnedEvent(
                orderId, "order-key-1", 50L, returnAmount, 80000L, 1000L, 3000L,
                true, 2500L, List.of(), returnId
        );
        EventEnvelope<OrderReturnedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.returned", "commerce-service",
                LocalDateTime.of(2026, 4, 9, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.returned", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("반품 완료 이벤트 수신 시 올바른 멱등성 키로 역분개를 기록하고 acknowledge한다")
    void handleOrderReturnedEvent_recordsPaymentCancellationWithIdempotencyKeyAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 60000L);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verify(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 60000L, "ORDER_RETURN:1");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("다른 orderId로 이벤트 수신 시 해당 orderId 기반 멱등성 키가 생성된다")
    void handleOrderReturnedEvent_differentOrderId_generatesCorrectIdempotencyKey() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(999L, 45000L);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verify(recordLedgerEntryUseCase).recordPaymentCancellation(999L, 45000L, "ORDER_RETURN:999");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 처리된 역분개에 대해 DuplicateLedgerTransactionException 발생 시 멱등 처리로 정상 acknowledge한다")
    void handleOrderReturnedEvent_duplicateTransaction_acknowledgesGracefully() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 60000L);
        doThrow(new DuplicateLedgerTransactionException("ORDER_RETURN:1"))
                .when(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 60000L, "ORDER_RETURN:1");

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verify(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 60000L, "ORDER_RETURN:1");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.returned", 0, 0L, "1", null
        );

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(recordLedgerEntryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        OrderReturnedEvent event = new OrderReturnedEvent(
                1L, "order-key-1", 50L, 60000L, 80000L, 1000L, 3000L,
                true, 2500L, List.of(), null
        );
        EventEnvelope<OrderReturnedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 4, 9, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.returned", 0, 0L, "1", envelope
        );

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(recordLedgerEntryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 60000L);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(recordLedgerEntryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, 60000L);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(recordLedgerEntryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, 60000L);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(recordLedgerEntryUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleOrderReturnedEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 60000L);
        doThrow(new RuntimeException("DB 연결 오류"))
                .when(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 60000L, "ORDER_RETURN:1");

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderReturnedEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 오류");

        verify(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 60000L, "ORDER_RETURN:1");
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("returnId 포함 이벤트 수신 시 idempotencyKey에 returnId가 포함된다")
    void handleOrderReturnedEvent_withReturnId_includesReturnIdInIdempotencyKey() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 60000L, "return-uuid-123");

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verify(recordLedgerEntryUseCase).recordPaymentCancellation(1L, 60000L, "ORDER_RETURN:1:return-uuid-123");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("returnId가 null인 이벤트 수신 시 기존 형식 idempotencyKey를 사용한다")
    void handleOrderReturnedEvent_nullReturnId_usesLegacyIdempotencyKey() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(200L, 40000L, null);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verify(recordLedgerEntryUseCase).recordPaymentCancellation(200L, 40000L, "ORDER_RETURN:200");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("동일 orderId에 다른 returnId로 부분반품/전체반품이 2회 발생하면 두 번째 역분개도 정상 호출된다")
    void handleOrderReturnedEvent_distinctReturnIds_bothReversalsInvoked() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> firstRecord = buildRecord(300L, 20000L, "return-1");
        ConsumerRecord<String, EventEnvelope<?>> secondRecord = buildRecord(300L, 50000L, "return-2");

        // when
        consumer.handleOrderReturnedEvent(firstRecord, acknowledgment);
        consumer.handleOrderReturnedEvent(secondRecord, acknowledgment);

        // then
        verify(recordLedgerEntryUseCase).recordPaymentCancellation(300L, 20000L, "ORDER_RETURN:300:return-1");
        verify(recordLedgerEntryUseCase).recordPaymentCancellation(300L, 50000L, "ORDER_RETURN:300:return-2");
        verify(acknowledgment, times(2)).acknowledge();
    }

    @Test
    @DisplayName("동일 returnId로 재발행되어 DuplicateLedgerTransactionException 발생 시 멱등 처리하고 acknowledge한다")
    void handleOrderReturnedEvent_duplicateReturnId_idempotentlyAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(400L, 30000L, "return-duplicate");
        doThrow(new DuplicateLedgerTransactionException("ORDER_RETURN:400:return-duplicate"))
                .when(recordLedgerEntryUseCase)
                .recordPaymentCancellation(400L, 30000L, "ORDER_RETURN:400:return-duplicate");

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }
}
