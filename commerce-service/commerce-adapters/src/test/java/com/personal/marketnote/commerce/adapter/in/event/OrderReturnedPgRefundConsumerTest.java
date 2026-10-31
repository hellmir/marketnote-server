package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnRefundCommand;
import com.personal.marketnote.commerce.port.in.usecase.returntracker.CompleteReturnRefundUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderReturnedEvent;
import com.personal.marketnote.common.kafka.event.OrderReturnedEvent.OrderProductItem;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderReturnedPgRefundConsumer 테스트")
class OrderReturnedPgRefundConsumerTest {

    @InjectMocks
    private OrderReturnedPgRefundConsumer consumer;

    @Mock
    private CompleteReturnRefundUseCase completeReturnRefundUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private static final UUID SHARER_KEY = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(
            Long orderId, String orderKey, boolean isFullReturn,
            Long returnAmount, Long paymentAmount, Long returnShippingFee
    ) {
        OrderReturnedEvent event = new OrderReturnedEvent(
                orderId, orderKey, 50L, returnAmount, paymentAmount, 1000L, 3000L,
                isFullReturn, returnShippingFee,
                List.of(new OrderProductItem(100L, SHARER_KEY, 2, 30000L)),
                "return-id-1"
        );
        EventEnvelope<OrderReturnedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.returned", "commerce-service",
                LocalDateTime.of(2026, 4, 20, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.returned", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("정상 반품 완료 이벤트 수신 시 CompleteReturnRefundCommand를 생성하여 UseCase를 호출하고 acknowledge한다")
    void handleOrderReturnedEvent_validEvent_invokesUseCaseAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "order-key-1", true, 60000L, 80000L, 2500L);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<CompleteReturnRefundCommand> captor = ArgumentCaptor.forClass(CompleteReturnRefundCommand.class);
        verify(completeReturnRefundUseCase).completeReturnRefund(captor.capture());

        CompleteReturnRefundCommand command = captor.getValue();
        assertThat(command.orderId()).isEqualTo(1L);
        assertThat(command.orderKey()).isEqualTo("order-key-1");
        assertThat(command.buyerId()).isEqualTo(50L);
        assertThat(command.returnAmount()).isEqualTo(60000L);
        assertThat(command.paymentAmount()).isEqualTo(80000L);
        assertThat(command.pointAmount()).isEqualTo(1000L);
        assertThat(command.shippingFee()).isEqualTo(3000L);
        assertThat(command.isFullReturn()).isTrue();
        assertThat(command.returnShippingFee()).isEqualTo(2500L);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("returnShippingFee가 null이면 0L로 기본값 적용하여 UseCase를 호출한다")
    void handleOrderReturnedEvent_nullReturnShippingFee_defaultsToZero() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "order-key-1", true, 60000L, 80000L, null);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<CompleteReturnRefundCommand> captor = ArgumentCaptor.forClass(CompleteReturnRefundCommand.class);
        verify(completeReturnRefundUseCase).completeReturnRefund(captor.capture());
        assertThat(captor.getValue().returnShippingFee()).isEqualTo(0L);

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
        verifyNoInteractions(completeReturnRefundUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        OrderReturnedEvent event = new OrderReturnedEvent(
                1L, "order-key-1", 50L, 60000L, 80000L, 1000L, 3000L,
                true, 2500L,
                List.of(new OrderProductItem(100L, SHARER_KEY, 2, 30000L)),
                "return-id-1"
        );
        EventEnvelope<OrderReturnedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 4, 20, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.returned", 0, 0L, "1", envelope
        );

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(completeReturnRefundUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, "order-key-1", true, 60000L, 80000L, 2500L);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(completeReturnRefundUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, "order-key-1", true, 60000L, 80000L, 2500L);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(completeReturnRefundUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("UseCase에서 예외가 발생해도 catch 후 acknowledge한다")
    void handleOrderReturnedEvent_useCaseThrows_acknowledgesGracefully() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "order-key-1", true, 60000L, 80000L, 2500L);
        doThrow(new RuntimeException("DB 연결 오류"))
                .when(completeReturnRefundUseCase).completeReturnRefund(any(CompleteReturnRefundCommand.class));

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verify(completeReturnRefundUseCase).completeReturnRefund(any(CompleteReturnRefundCommand.class));
        verify(acknowledgment).acknowledge();
    }
}
