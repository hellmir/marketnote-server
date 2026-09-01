package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ShippingStatusChangedEvent;
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

import static com.personal.marketnote.commerce.domain.order.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShippingStatusChangedOrderStatusConsumer 테스트")
class ShippingStatusChangedOrderStatusConsumerTest {
    @InjectMocks
    private ShippingStatusChangedOrderStatusConsumer consumer;

    @Mock
    private ChangeOrderStatusUseCase changeOrderStatusUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, String shippingStatus) {
        ShippingStatusChangedEvent event = new ShippingStatusChangedEvent(
                orderId, shippingStatus, "TRACK-001", "CJ", LocalDateTime.of(2026, 4, 9, 10, 0)
        );
        EventEnvelope<ShippingStatusChangedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "fulfillment.shipping.status-changed", "fulfillment-service",
                LocalDateTime.of(2026, 4, 9, 10, 0), event
        );
        return new ConsumerRecord<>("fulfillment.shipping.status-changed", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("배송 상태 SHIPPING 이벤트 수신 시 주문 상태를 SHIPPING으로 변경하고 acknowledge한다")
    void handleShippingStatusChangedEvent_shipping_changesOrderStatusToShippingAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "SHIPPING");

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ChangeOrderStatusCommand> captor = ArgumentCaptor.forClass(ChangeOrderStatusCommand.class);
        verify(changeOrderStatusUseCase).changeOrderStatus(captor.capture());

        ChangeOrderStatusCommand command = captor.getValue();
        assertThat(command.id()).isEqualTo(1L);
        assertThat(command.orderStatus()).isEqualTo(SHIPPING);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("배송 상태 DELIVERED 이벤트 수신 시 주문 상태를 DELIVERED로 변경하고 acknowledge한다")
    void handleShippingStatusChangedEvent_delivered_changesOrderStatusToDeliveredAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "DELIVERED");

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ChangeOrderStatusCommand> captor = ArgumentCaptor.forClass(ChangeOrderStatusCommand.class);
        verify(changeOrderStatusUseCase).changeOrderStatus(captor.capture());

        ChangeOrderStatusCommand command = captor.getValue();
        assertThat(command.id()).isEqualTo(1L);
        assertThat(command.orderStatus()).isEqualTo(DELIVERED);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("배송 상태 RETURN_SHIPPING 이벤트 수신 시 주문 상태를 RETURN_IN_PROGRESS로 변경하고 acknowledge한다")
    void handleShippingStatusChangedEvent_returnShipping_changesOrderStatusToReturnInProgressAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "RETURN_SHIPPING");

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ChangeOrderStatusCommand> captor = ArgumentCaptor.forClass(ChangeOrderStatusCommand.class);
        verify(changeOrderStatusUseCase).changeOrderStatus(captor.capture());

        ChangeOrderStatusCommand command = captor.getValue();
        assertThat(command.id()).isEqualTo(1L);
        assertThat(command.orderStatus()).isEqualTo(RETURN_IN_PROGRESS);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("배송 상태 RETURN_DELIVERED 이벤트 수신 시 주문 상태를 RETURNED로 변경하고 acknowledge한다")
    void handleShippingStatusChangedEvent_returnDelivered_changesOrderStatusToReturnedAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "RETURN_DELIVERED");

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ChangeOrderStatusCommand> captor = ArgumentCaptor.forClass(ChangeOrderStatusCommand.class);
        verify(changeOrderStatusUseCase).changeOrderStatus(captor.capture());

        ChangeOrderStatusCommand command = captor.getValue();
        assertThat(command.id()).isEqualTo(1L);
        assertThat(command.orderStatus()).isEqualTo(RETURNED);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("매핑되지 않는 배송 상태(PREPARING) 이벤트 수신 시 UseCase를 호출하지 않고 acknowledge한다")
    void handleShippingStatusChangedEvent_preparing_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "PREPARING");

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("매핑되지 않는 배송 상태(CANCELLED) 이벤트 수신 시 UseCase를 호출하지 않고 acknowledge한다")
    void handleShippingStatusChangedEvent_cancelled_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "CANCELLED");

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 해당 상태인 주문에 대해 OrderStatusAlreadyChangedException 발생 시 멱등 처리하고 acknowledge한다")
    void handleShippingStatusChangedEvent_alreadyChanged_acknowledgesGracefully() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "SHIPPING");
        doThrow(new OrderStatusAlreadyChangedException(SHIPPING))
                .when(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verify(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("전이 불가 상태에서 InvalidOrderStatusTransitionException 발생 시 무시하고 acknowledge한다")
    void handleShippingStatusChangedEvent_invalidTransition_acknowledgesGracefully() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "DELIVERED");
        doThrow(new InvalidOrderStatusTransitionException(CANCELLED, DELIVERED))
                .when(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verify(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleShippingStatusChangedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, "SHIPPING");

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleShippingStatusChangedEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, "SHIPPING");

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleShippingStatusChangedEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, "SHIPPING");

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleShippingStatusChangedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "fulfillment.shipping.status-changed", 0, 0L, "1", null
        );

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleShippingStatusChangedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        ShippingStatusChangedEvent event = new ShippingStatusChangedEvent(
                1L, "SHIPPING", "TRACK-001", "CJ", LocalDateTime.of(2026, 4, 9, 10, 0)
        );
        EventEnvelope<ShippingStatusChangedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "fulfillment-service",
                LocalDateTime.of(2026, 4, 9, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "fulfillment.shipping.status-changed", 0, 0L, "1", envelope
        );

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("shippingStatus가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleShippingStatusChangedEvent_nullShippingStatus_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null);

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleShippingStatusChangedEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "SHIPPING");
        doThrow(new RuntimeException("DB 연결 오류"))
                .when(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handleShippingStatusChangedEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 오류");

        verify(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }
}
