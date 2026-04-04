package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent.OrderProductItem;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingSharedPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingSharedPointUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCancelledPartialSharedPointConsumer 테스트")
class PaymentCancelledPartialSharedPointConsumerTest {
    private static final UUID SHARER_KEY_1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID SHARER_KEY_2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    private static final UUID SHARER_KEY_3 = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");

    @InjectMocks
    private PaymentCancelledPartialSharedPointConsumer consumer;

    @Mock
    private ModifyPendingSharedPointUseCase modifyPendingSharedPointUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(consumer, "sharePointRate", 0.1f);
    }

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, boolean isFullCancel,
                                                                 Long cancelAmount, Long paymentAmount,
                                                                 List<OrderProductItem> orderProducts) {
        PaymentCancelledEvent event = new PaymentCancelledEvent(
                orderId, "order-key-1", 100L, cancelAmount, paymentAmount, 1000L,
                isFullCancel, 0L, null, orderProducts, null, null
        );
        EventEnvelope<PaymentCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.payment.cancelled", "commerce-service",
                LocalDateTime.of(2026, 3, 7, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.payment.cancelled", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("부분 취소 이벤트 수신 시 공유자가 있으면 비례 차감 포인트를 차감하고 acknowledge한다")
    void handlePaymentCancelledEvent_partialCancelWithSharers_deductsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L),
                new OrderProductItem(2L, SHARER_KEY_2, 1, 20000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, 30000L, 80000L, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ModifyPendingSharedPointCommand> captor = ArgumentCaptor.forClass(ModifyPendingSharedPointCommand.class);
        verify(modifyPendingSharedPointUseCase, times(2)).modifyPending(captor.capture());

        List<ModifyPendingSharedPointCommand> commands = captor.getAllValues();
        long expectedOriginalSharePoint = Math.round(80000L * 0.1f);
        long expectedProportionalPoint = (30000L * expectedOriginalSharePoint + 80000L / 2) / 80000L;

        ModifyPendingSharedPointCommand firstCommand = commands.get(0);
        assertThat(firstCommand.sharerKey()).isEqualTo(SHARER_KEY_1);
        assertThat(firstCommand.changeType()).isEqualTo(UserPointChangeType.DEDUCTION);
        assertThat(firstCommand.amount()).isEqualTo(expectedProportionalPoint);
        assertThat(firstCommand.sourceType()).isEqualTo(UserPointSourceType.ORDER);
        assertThat(firstCommand.sourceId()).isEqualTo(1L);
        assertThat(firstCommand.reason()).isEqualTo("부분 결제 취소 공유 적립 예정 포인트 차감");

        ModifyPendingSharedPointCommand secondCommand = commands.get(1);
        assertThat(secondCommand.sharerKey()).isEqualTo(SHARER_KEY_2);
        assertThat(secondCommand.changeType()).isEqualTo(UserPointChangeType.DEDUCTION);
        assertThat(secondCommand.amount()).isEqualTo(expectedProportionalPoint);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("전체 취소 이벤트이면 부분 공유 적립 예정 포인트 차감 없이 acknowledge한다")
    void handlePaymentCancelledEvent_fullCancel_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, 50000L, 80000L, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 부분 공유 적립 예정 포인트 차감 없이 acknowledge한다")
    void handlePaymentCancelledEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, false, 30000L, 80000L, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("공유자가 없는 주문이면 부분 공유 적립 예정 포인트 차감 없이 acknowledge한다")
    void handlePaymentCancelledEvent_noSharers_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, null, 2, 10000L),
                new OrderProductItem(2L, null, 1, 20000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, 30000L, 80000L, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderProducts가 빈 목록이면 부분 공유 적립 예정 포인트 차감 없이 acknowledge한다")
    void handlePaymentCancelledEvent_emptyOrderProducts_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, 30000L, 80000L, List.of());

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handlePaymentCancelledEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        PaymentCancelledEvent event = new PaymentCancelledEvent(
                1L, "order-key-1", 100L, 30000L, 80000L, 1000L,
                false, 0L, null, orderProducts, null, null
        );
        EventEnvelope<PaymentCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 3, 7, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.cancelled", 0, 0L, "1", envelope
        );

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handlePaymentCancelledEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, false, 30000L, 80000L, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handlePaymentCancelledEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, false, 30000L, 80000L, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 부분 공유 적립 예정 포인트 차감 없이 acknowledge한다")
    void handlePaymentCancelledEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.cancelled", 0, 0L, "1", null
        );

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("paymentAmount가 null이면 비례 차감 계산 불가로 acknowledge한다")
    void handlePaymentCancelledEvent_nullPaymentAmount_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, 30000L, null, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("paymentAmount가 0이면 비례 차감 계산 불가로 acknowledge한다")
    void handlePaymentCancelledEvent_zeroPaymentAmount_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, 30000L, 0L, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("cancelAmount가 null이면 비례 차감 계산 불가로 acknowledge한다")
    void handlePaymentCancelledEvent_nullCancelAmount_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, null, 80000L, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("cancelAmount가 0이면 비례 차감 계산 불가로 acknowledge한다")
    void handlePaymentCancelledEvent_zeroCancelAmount_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, 0L, 80000L, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("cancelAmount가 paymentAmount보다 크면 차감 없이 acknowledge한다")
    void handlePaymentCancelledEvent_cancelExceedsPayment_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, 100000L, 80000L, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("중복 공유자가 있으면 중복 제거 후 차감하고 acknowledge한다")
    void handlePaymentCancelledEvent_duplicateSharers_deduplicatesAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L),
                new OrderProductItem(2L, SHARER_KEY_1, 1, 20000L),
                new OrderProductItem(3L, SHARER_KEY_2, 1, 15000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, 30000L, 80000L, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(modifyPendingSharedPointUseCase, times(2)).modifyPending(any(ModifyPendingSharedPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("중복 이벤트 수신 시 DuplicateUserPointHistoryException이 발생하면 멱등 처리하고 acknowledge한다")
    void handlePaymentCancelledEvent_duplicateEvent_idempotentAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, 30000L, 80000L, orderProducts);
        doThrow(new DuplicateUserPointHistoryException(200L, UserPointSourceType.ORDER, 1L, "부분 결제 취소 공유 적립 예정 포인트 차감"))
                .when(modifyPendingSharedPointUseCase).modifyPending(any(ModifyPendingSharedPointCommand.class));

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(modifyPendingSharedPointUseCase).modifyPending(any(ModifyPendingSharedPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("공유자 2명 중 첫 번째만 중복이면 두 번째는 정상 차감하고 acknowledge한다")
    void handlePaymentCancelledEvent_partialDuplicate_continuesAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L),
                new OrderProductItem(2L, SHARER_KEY_2, 1, 20000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, 30000L, 80000L, orderProducts);
        when(modifyPendingSharedPointUseCase.modifyPending(any(ModifyPendingSharedPointCommand.class)))
                .thenThrow(new DuplicateUserPointHistoryException(200L, UserPointSourceType.ORDER, 1L, "부분 결제 취소 공유 적립 예정 포인트 차감"))
                .thenReturn(null);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(modifyPendingSharedPointUseCase, times(2)).modifyPending(any(ModifyPendingSharedPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("공유자 전원 중복이면 모두 멱등 처리하고 acknowledge한다")
    void handlePaymentCancelledEvent_allDuplicate_idempotentAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L),
                new OrderProductItem(2L, SHARER_KEY_2, 1, 20000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, 30000L, 80000L, orderProducts);
        when(modifyPendingSharedPointUseCase.modifyPending(any(ModifyPendingSharedPointCommand.class)))
                .thenThrow(new DuplicateUserPointHistoryException(200L, UserPointSourceType.ORDER, 1L, "부분 결제 취소 공유 적립 예정 포인트 차감"))
                .thenThrow(new DuplicateUserPointHistoryException(300L, UserPointSourceType.ORDER, 1L, "부분 결제 취소 공유 적립 예정 포인트 차감"));

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(modifyPendingSharedPointUseCase, times(2)).modifyPending(any(ModifyPendingSharedPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 acknowledge하지 않고 예외를 전파한다")
    void handlePaymentCancelledEvent_unexpectedException_propagatesWithoutAcknowledge() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, 30000L, 80000L, orderProducts);
        doThrow(new RuntimeException("DB 연결 실패"))
                .when(modifyPendingSharedPointUseCase).modifyPending(any(ModifyPendingSharedPointCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handlePaymentCancelledEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 실패");

        verify(modifyPendingSharedPointUseCase).modifyPending(any(ModifyPendingSharedPointCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외(잘못된 페이로드) 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    @SuppressWarnings("unchecked")
    void handlePaymentCancelledEvent_invalidPayload_propagatesForRetry() {
        // given
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.payment.cancelled", "commerce-service",
                LocalDateTime.of(2026, 3, 7, 10, 0), "invalid-payload"
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.cancelled", 0, 0L, "1",
                (EventEnvelope<?>) (EventEnvelope) envelope
        );

        // when & then
        assertThatThrownBy(() -> consumer.handlePaymentCancelledEvent(record, acknowledgment))
                .isInstanceOf(Exception.class);

        verifyNoInteractions(modifyPendingSharedPointUseCase);
        verify(acknowledgment, never()).acknowledge();
    }
}
