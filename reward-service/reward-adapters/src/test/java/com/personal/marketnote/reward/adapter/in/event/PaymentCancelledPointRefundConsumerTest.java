package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCancelledPointRefundConsumer 테스트")
class PaymentCancelledPointRefundConsumerTest {
    @InjectMocks
    private PaymentCancelledPointRefundConsumer consumer;

    @Mock
    private ModifyUserPointUseCase modifyUserPointUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, Long buyerId,
                                                                  Long pointAmount, boolean isFullCancel) {
        PaymentCancelledEvent event = new PaymentCancelledEvent(
                orderId, "order-key-1", buyerId, 50000L, 80000L, pointAmount,
                isFullCancel, 0L, List.of(), null, null
        );
        EventEnvelope<PaymentCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.payment.cancelled", "commerce-service",
                LocalDateTime.of(2026, 3, 6, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.payment.cancelled", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("전체 취소 이벤트 수신 시 포인트를 환불하고 acknowledge한다")
    void handlePaymentCancelledEvent_fullCancel_refundsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 1000L, true);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ModifyUserPointCommand> captor = ArgumentCaptor.forClass(ModifyUserPointCommand.class);
        verify(modifyUserPointUseCase).modify(captor.capture());
        ModifyUserPointCommand command = captor.getValue();

        assertThat(command.userId()).isEqualTo(100L);
        assertThat(command.changeType()).isEqualTo(UserPointChangeType.ACCRUAL);
        assertThat(command.amount()).isEqualTo(1000L);
        assertThat(command.sourceType()).isEqualTo(UserPointSourceType.ORDER);
        assertThat(command.sourceId()).isEqualTo(1L);
        assertThat(command.reason()).isEqualTo("주문 취소 포인트 환불");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("부분 취소 이벤트이면 포인트 환불 없이 acknowledge한다")
    void handlePaymentCancelledEvent_partialCancel_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 1000L, false);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 포인트 환불 없이 acknowledge한다")
    void handlePaymentCancelledEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 100L, 1000L, true);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 null이면 포인트 환불 없이 acknowledge한다")
    void handlePaymentCancelledEvent_nullBuyerId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null, 1000L, true);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("pointAmount가 null이면 포인트 환불 없이 acknowledge한다")
    void handlePaymentCancelledEvent_nullPointAmount_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, null, true);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("pointAmount가 0이면 포인트 환불 없이 acknowledge한다")
    void handlePaymentCancelledEvent_zeroPointAmount_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 0L, true);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("pointAmount가 음수이면 포인트 환불 없이 acknowledge한다")
    void handlePaymentCancelledEvent_negativePointAmount_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, -500L, true);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 포인트 환불 없이 acknowledge한다")
    void handlePaymentCancelledEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.cancelled", 0, 0L, "1", null
        );

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("중복 이벤트 수신 시 DuplicateUserPointHistoryException이 발생하면 멱등 처리하고 acknowledge한다")
    void handlePaymentCancelledEvent_duplicateEvent_idempotentAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 1000L, true);
        doThrow(new DuplicateUserPointHistoryException(100L, UserPointSourceType.ORDER, 1L, "주문 취소 포인트 환불"))
                .when(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("UseCase에서 예상치 못한 예외 발생 시 acknowledge하지 않고 예외를 전파한다")
    void handlePaymentCancelledEvent_useCaseUnexpectedException_propagatesWithoutAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 1000L, true);
        doThrow(new RuntimeException("DB 연결 실패"))
                .when(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handlePaymentCancelledEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 실패");

        verify(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));
        verify(acknowledgment, never()).acknowledge();
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
