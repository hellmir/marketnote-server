package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
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

import static com.personal.marketnote.commerce.domain.order.OrderStatus.CANCELLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCancelledOrderStatusConsumer 테스트")
class PaymentCancelledOrderStatusConsumerTest {
    @InjectMocks
    private PaymentCancelledOrderStatusConsumer consumer;

    @Mock
    private ChangeOrderStatusUseCase changeOrderStatusUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, String orderKey,
                                                                 boolean isFullCancel, Long cancelAmount) {
        PaymentCancelledEvent event = new PaymentCancelledEvent(
                orderId, orderKey, 100L, cancelAmount, 50000L, 1000L,
                isFullCancel, 0L, null, List.of(), null, null
        );
        EventEnvelope<PaymentCancelledEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.payment.cancelled", "commerce-service",
                LocalDateTime.of(2026, 3, 6, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.payment.cancelled", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("전체 취소 이벤트 수신 시 주문 상태를 CANCELLED로 변경하고 acknowledge한다")
    void handlePaymentCancelledEvent_fullCancel_changesOrderStatusToCancelledAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "order-key-1", true, 50000L);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ChangeOrderStatusCommand> captor = ArgumentCaptor.forClass(ChangeOrderStatusCommand.class);
        verify(changeOrderStatusUseCase).changeOrderStatus(captor.capture());

        ChangeOrderStatusCommand command = captor.getValue();
        assertThat(command.id()).isEqualTo(1L);
        assertThat(command.orderStatus()).isEqualTo(CANCELLED);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("부분 취소 이벤트(isFullCancel=false) 수신 시 상태 변경 없이 acknowledge한다")
    void handlePaymentCancelledEvent_partialCancel_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "order-key-1", false, 10000L);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handlePaymentCancelledEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, "order-key-1", true, 50000L);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 상태가 변경된 주문에 대해 OrderStatusAlreadyChangedException 발생 시 멱등 처리로 정상 acknowledge한다")
    void handlePaymentCancelledEvent_alreadyChanged_acknowledgesGracefully() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "order-key-1", true, 50000L);
        doThrow(new OrderStatusAlreadyChangedException(CANCELLED))
                .when(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verify(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handlePaymentCancelledEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.payment.cancelled", 0, 0L, "1", null
        );

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handlePaymentCancelledEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        PaymentCancelledEvent event = new PaymentCancelledEvent(
                1L, "order-key-1", 100L, 50000L, 50000L, 1000L,
                true, 0L, null, List.of(), null, null
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
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handlePaymentCancelledEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, "order-key-1", true, 50000L);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handlePaymentCancelledEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, "order-key-1", true, 50000L);

        // when
        consumer.handlePaymentCancelledEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handlePaymentCancelledEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "order-key-1", true, 50000L);
        doThrow(new RuntimeException("DB 연결 오류"))
                .when(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handlePaymentCancelledEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 오류");

        verify(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }
}
