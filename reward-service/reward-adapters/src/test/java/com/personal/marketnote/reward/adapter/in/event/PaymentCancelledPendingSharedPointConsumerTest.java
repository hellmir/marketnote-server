package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent.OrderProductItem;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointSnapshotState;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.port.in.command.point.CancelPendingPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.CancelPendingPointUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCancelledPendingSharedPointConsumer 테스트")
class PaymentCancelledPendingSharedPointConsumerTest {
    private static final UUID SHARER_KEY_1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID SHARER_KEY_2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    private static final UUID SHARER_KEY_3 = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");

    @InjectMocks
    private PaymentCancelledPendingSharedPointConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CancelPendingPointUseCase cancelPendingPointUseCase;

    @Mock
    private FindUserPointPort findUserPointPort;

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, boolean isFullCancel,
                                                                 List<OrderProductItem> orderProducts) {
        PaymentCancelledEvent event = new PaymentCancelledEvent(
                orderId, "order-key-1", 100L, 50000L, 80000L, 1000L,
                isFullCancel, 0L, null, orderProducts, null, null
        );
        EventEnvelope<PaymentCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.payment.cancelled", "commerce-service",
                LocalDateTime.of(2026, 3, 6, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.payment.cancelled", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("전체 취소 이벤트 수신 시 공유자별 적립 예정 포인트를 회수하고 acknowledge한다")
    void handlePaymentCancelledEvent_fullCancelWithSharers_cancelsPendingPointsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L),
                new OrderProductItem(2L, SHARER_KEY_2, 1, 20000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, orderProducts);

        UserPoint userPoint1 = UserPoint.from(UserPointSnapshotState.builder()
                .userId(200L).userKey(SHARER_KEY_1.toString()).amount(0L)
                .addExpectedAmount(0L).expireExpectedAmount(0L).build());
        UserPoint userPoint2 = UserPoint.from(UserPointSnapshotState.builder()
                .userId(300L).userKey(SHARER_KEY_2.toString()).amount(0L)
                .addExpectedAmount(0L).expireExpectedAmount(0L).build());
        when(findUserPointPort.findByUserKey(SHARER_KEY_1.toString())).thenReturn(Optional.of(userPoint1));
        when(findUserPointPort.findByUserKey(SHARER_KEY_2.toString())).thenReturn(Optional.of(userPoint2));

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        CancelPendingPointCommand expectedCommand200 = CancelPendingPointCommand.builder()
                .userId(200L)
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(1L)
                .reason("결제 취소 적립 예정 포인트 회수")
                .build();
        CancelPendingPointCommand expectedCommand300 = CancelPendingPointCommand.builder()
                .userId(300L)
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(1L)
                .reason("결제 취소 적립 예정 포인트 회수")
                .build();
        verify(cancelPendingPointUseCase).cancelPending(expectedCommand200);
        verify(cancelPendingPointUseCase).cancelPending(expectedCommand300);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("부분 취소 이벤트이면 공유 적립 예정 포인트 회수 없이 acknowledge한다")
    void handlePaymentCancelledEvent_partialCancel_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, false, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(cancelPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 공유 적립 예정 포인트 회수 없이 acknowledge한다")
    void handlePaymentCancelledEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, true, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(cancelPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("공유자가 없는 주문이면 공유 적립 예정 포인트 회수 없이 acknowledge한다")
    void handlePaymentCancelledEvent_noSharers_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, null, 2, 10000L),
                new OrderProductItem(2L, null, 1, 20000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(cancelPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderProducts가 빈 목록이면 공유 적립 예정 포인트 회수 없이 acknowledge한다")
    void handlePaymentCancelledEvent_emptyOrderProducts_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, List.of());

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(cancelPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 acknowledge한다")
    void handlePaymentCancelledEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        PaymentCancelledEvent event = new PaymentCancelledEvent(
                1L, "order-key-1", 100L, 50000L, 80000L, 1000L,
                true, 0L, null, orderProducts, null, null
        );
        EventEnvelope<PaymentCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 3, 6, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.cancelled", 0, 0L, "1", envelope
        );

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(cancelPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 acknowledge한다")
    void handlePaymentCancelledEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, true, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(cancelPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 acknowledge한다")
    void handlePaymentCancelledEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, true, orderProducts);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(cancelPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 공유 적립 예정 포인트 회수 없이 acknowledge한다")
    void handlePaymentCancelledEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.cancelled", 0, 0L, "1", null
        );

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(cancelPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("중복 공유자가 있으면 중복 제거 후 공유자별 적립 예정 포인트를 회수한다")
    void handlePaymentCancelledEvent_duplicateSharers_deduplicatesAndCancelsPendingPoints() {
        // given
        List<OrderProductItem> orderProducts = List.of(
                new OrderProductItem(1L, SHARER_KEY_1, 2, 10000L),
                new OrderProductItem(2L, SHARER_KEY_1, 1, 20000L),
                new OrderProductItem(3L, SHARER_KEY_2, 1, 15000L)
        );
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, true, orderProducts);

        UserPoint userPoint1 = UserPoint.from(UserPointSnapshotState.builder()
                .userId(200L).userKey(SHARER_KEY_1.toString()).amount(0L)
                .addExpectedAmount(0L).expireExpectedAmount(0L).build());
        UserPoint userPoint2 = UserPoint.from(UserPointSnapshotState.builder()
                .userId(300L).userKey(SHARER_KEY_2.toString()).amount(0L)
                .addExpectedAmount(0L).expireExpectedAmount(0L).build());
        when(findUserPointPort.findByUserKey(SHARER_KEY_1.toString())).thenReturn(Optional.of(userPoint1));
        when(findUserPointPort.findByUserKey(SHARER_KEY_2.toString())).thenReturn(Optional.of(userPoint2));

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(cancelPendingPointUseCase, times(2)).cancelPending(any(CancelPendingPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    @SuppressWarnings("unchecked")
    void handlePaymentCancelledEvent_unexpectedException_propagatesForRetry() {
        // given
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.payment.cancelled", "commerce-service",
                LocalDateTime.of(2026, 3, 6, 10, 0), "invalid-payload"
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.cancelled", 0, 0L, "1",
                (EventEnvelope<?>) (EventEnvelope) envelope
        );

        // when & then
        assertThatThrownBy(() -> consumer.handlePaymentCancelledEvent(record, acknowledgment))
                .isInstanceOf(Exception.class);

        verify(acknowledgment, never()).acknowledge();
    }
}
