package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPurchaseConfirmedEvent;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.port.in.command.point.ConfirmPendingPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ConfirmPendingPointUseCase;
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
@DisplayName("OrderPurchaseConfirmedProductPointConsumer 테스트")
class OrderPurchaseConfirmedProductPointConsumerTest {
    @InjectMocks
    private OrderPurchaseConfirmedProductPointConsumer consumer;

    @Mock
    private ConfirmPendingPointUseCase confirmPendingPointUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, Long buyerId, List<UUID> sharerKeys) {
        OrderPurchaseConfirmedEvent event = new OrderPurchaseConfirmedEvent(orderId, buyerId, sharerKeys);
        EventEnvelope<OrderPurchaseConfirmedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.purchase-confirmed", "commerce-service",
                LocalDateTime.of(2026, 3, 8, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.purchase-confirmed", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("구매 확정 이벤트 수신 시 적립 예정 포인트를 확정하고 acknowledge한다")
    void handleOrderPurchaseConfirmedEvent_validEvent_confirmsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, List.of());

        // when
        consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ConfirmPendingPointCommand> captor = ArgumentCaptor.forClass(ConfirmPendingPointCommand.class);
        verify(confirmPendingPointUseCase).confirmPending(captor.capture());

        ConfirmPendingPointCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(100L);
        assertThat(command.sourceType()).isEqualTo(UserPointSourceType.ORDER);
        assertThat(command.sourceId()).isEqualTo(1L);
        assertThat(command.reason()).isEqualTo("구매 확정 포인트 적립");

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 확정 없이 acknowledge한다")
    void handleOrderPurchaseConfirmedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 100L, List.of());

        // when
        consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(confirmPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 null이면 확정 없이 acknowledge한다")
    void handleOrderPurchaseConfirmedEvent_nullBuyerId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null, List.of());

        // when
        consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(confirmPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPurchaseConfirmedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        OrderPurchaseConfirmedEvent event = new OrderPurchaseConfirmedEvent(1L, 100L, List.<UUID>of());
        EventEnvelope<OrderPurchaseConfirmedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 3, 8, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.purchase-confirmed", 0, 0L, "1", envelope
        );

        // when
        consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(confirmPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPurchaseConfirmedEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, 100L, List.of());

        // when
        consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(confirmPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPurchaseConfirmedEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, 100L, List.of());

        // when
        consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(confirmPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPurchaseConfirmedEvent_zeroBuyerId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 0L, List.of());

        // when
        consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(confirmPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderPurchaseConfirmedEvent_negativeBuyerId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, -1L, List.of());

        // when
        consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(confirmPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 확정 없이 acknowledge한다")
    void handleOrderPurchaseConfirmedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.purchase-confirmed", 0, 0L, "1", null
        );

        // when
        consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(confirmPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 acknowledge하지 않고 예외를 전파한다")
    void handleOrderPurchaseConfirmedEvent_unexpectedException_propagatesWithoutAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, List.of());
        doThrow(new RuntimeException("DB 연결 실패"))
                .when(confirmPendingPointUseCase).confirmPending(any(ConfirmPendingPointCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 실패");

        verify(confirmPendingPointUseCase).confirmPending(any(ConfirmPendingPointCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외(잘못된 페이로드) 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    @SuppressWarnings("unchecked")
    void handleOrderPurchaseConfirmedEvent_invalidPayload_propagatesForRetry() {
        // given
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.purchase-confirmed", "commerce-service",
                LocalDateTime.of(2026, 3, 8, 10, 0), "invalid-payload"
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.purchase-confirmed", 0, 0L, "1",
                (EventEnvelope<?>) (EventEnvelope) envelope
        );

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderPurchaseConfirmedEvent(record, acknowledgment))
                .isInstanceOf(Exception.class);

        verifyNoInteractions(confirmPendingPointUseCase);
        verify(acknowledgment, never()).acknowledge();
    }
}
