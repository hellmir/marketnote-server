package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.PaymentAlreadyRefundedException;
import com.personal.marketnote.commerce.port.in.command.payment.RefundPaymentCommand;
import com.personal.marketnote.commerce.port.in.usecase.payment.RefundPaymentUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderCancelledEvent;
import com.personal.marketnote.common.kafka.event.OrderCancelledEvent.OrderProductItem;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancelledPgRefundConsumer 테스트")
class OrderCancelledPgRefundConsumerTest {
    @InjectMocks
    private OrderCancelledPgRefundConsumer consumer;

    @Mock
    private RefundPaymentUseCase refundPaymentUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private static final UUID SHARER_KEY = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, String orderKey,
                                                                 boolean isFullCancel, Long cancelAmount,
                                                                 Long paymentAmount, Long alreadyRefunded) {
        OrderCancelledEvent event = new OrderCancelledEvent(
                orderId, orderKey, 50L, cancelAmount, paymentAmount, 1000L, 3000L,
                isFullCancel, alreadyRefunded,
                List.of(new OrderProductItem(100L, SHARER_KEY, 2, 30000L)),
                List.of(new OrderProductItem(100L, SHARER_KEY, 2, 30000L))
        );
        EventEnvelope<OrderCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.cancelled", "commerce-service",
                LocalDateTime.of(2026, 4, 8, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.cancelled", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("전체 취소 이벤트 수신 시 RefundPaymentCommand를 생성하여 환불을 요청하고 acknowledge한다")
    void handleOrderCancelledEvent_fullCancel_refundsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "order-key-1", true, 60000L, 80000L, 0L);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        ArgumentCaptor<RefundPaymentCommand> captor = ArgumentCaptor.forClass(RefundPaymentCommand.class);
        verify(refundPaymentUseCase).refund(captor.capture());

        RefundPaymentCommand command = captor.getValue();
        assertThat(command.orderKey()).isEqualTo("order-key-1");
        assertThat(command.orderId()).isEqualTo(1L);
        assertThat(command.cancelAmount()).isEqualTo(60000L);
        assertThat(command.paymentAmount()).isEqualTo(80000L);
        assertThat(command.isFullCancel()).isTrue();
        assertThat(command.alreadyRefunded()).isEqualTo(0L);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("부분 취소 이벤트 수신 시 isFullCancel=false로 RefundPaymentCommand를 생성하여 환불을 요청하고 acknowledge한다")
    void handleOrderCancelledEvent_partialCancel_refundsWithPartialFlagAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(2L, "order-key-2", false, 30000L, 80000L, 20000L);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        ArgumentCaptor<RefundPaymentCommand> captor = ArgumentCaptor.forClass(RefundPaymentCommand.class);
        verify(refundPaymentUseCase).refund(captor.capture());

        RefundPaymentCommand command = captor.getValue();
        assertThat(command.orderKey()).isEqualTo("order-key-2");
        assertThat(command.orderId()).isEqualTo(2L);
        assertThat(command.cancelAmount()).isEqualTo(30000L);
        assertThat(command.isFullCancel()).isFalse();
        assertThat(command.alreadyRefunded()).isEqualTo(20000L);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 환불된 결제에 대해 PaymentAlreadyRefundedException 발생 시 멱등 처리로 정상 acknowledge한다")
    void handleOrderCancelledEvent_alreadyRefunded_acknowledgesGracefully() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "order-key-1", true, 60000L, 80000L, 0L);
        doThrow(new PaymentAlreadyRefundedException("order-key-1"))
                .when(refundPaymentUseCase).refund(any(RefundPaymentCommand.class));

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verify(refundPaymentUseCase).refund(any(RefundPaymentCommand.class));
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
        verifyNoInteractions(refundPaymentUseCase);
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
        verifyNoInteractions(refundPaymentUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, "order-key-1", true, 60000L, 80000L, 0L);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(refundPaymentUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, "order-key-1", true, 60000L, 80000L, 0L);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(refundPaymentUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderCancelledEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, "order-key-1", true, 60000L, 80000L, 0L);

        // when
        consumer.handleOrderCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(refundPaymentUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleOrderCancelledEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "order-key-1", true, 60000L, 80000L, 0L);
        doThrow(new RuntimeException("DB 연결 오류"))
                .when(refundPaymentUseCase).refund(any(RefundPaymentCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderCancelledEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 오류");

        verify(refundPaymentUseCase).refund(any(RefundPaymentCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }
}
