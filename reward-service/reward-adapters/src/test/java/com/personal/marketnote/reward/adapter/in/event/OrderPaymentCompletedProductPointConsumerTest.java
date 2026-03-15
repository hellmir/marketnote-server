package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingPointUseCase;
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
@DisplayName("OrderPaymentCompletedProductPointConsumer 테스트")
class OrderPaymentCompletedProductPointConsumerTest {
    @InjectMocks
    private OrderPaymentCompletedProductPointConsumer consumer;

    @Mock
    private ModifyPendingPointUseCase modifyPendingPointUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(
            Long orderId, Long buyerId, Long totalAmount, Long totalAccumulatedPoint
    ) {
        OrderPaymentCompletedEvent event = new OrderPaymentCompletedEvent(
                orderId, buyerId, totalAmount, 0L, List.of(), totalAccumulatedPoint
        );
        EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.payment-completed", "commerce-service",
                LocalDateTime.of(2026, 3, 8, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.payment-completed", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("상품 적립 포인트가 있는 주문 결제 완료 이벤트 수신 시 적립 예정 포인트를 적립하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_withAccumulatedPoint_accumulatesAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, 1500L);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ModifyPendingPointCommand> captor = ArgumentCaptor.forClass(ModifyPendingPointCommand.class);
        verify(modifyPendingPointUseCase).modifyPending(captor.capture());

        ModifyPendingPointCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(100L);
        assertThat(command.changeType()).isEqualTo(UserPointChangeType.ACCRUAL);
        assertThat(command.amount()).isEqualTo(1500L);
        assertThat(command.sourceType()).isEqualTo(UserPointSourceType.ORDER);
        assertThat(command.sourceId()).isEqualTo(1L);
        assertThat(command.reason()).isEqualTo("상품 구매 적립");

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 100L, 50000L, 1500L);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 null이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullBuyerId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null, 50000L, 1500L);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("totalAmount가 null이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullTotalAmount_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, null, 1500L);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("totalAmount가 0이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_zeroTotalAmount_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 0L, 1500L);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("totalAccumulatedPoint가 null이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_nullAccumulatedPoint_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, null);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("totalAccumulatedPoint가 0이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_zeroAccumulatedPoint_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, 0L);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("totalAccumulatedPoint가 음수이면 적립 없이 acknowledge한다")
    void handleOrderPaymentCompletedEvent_negativeAccumulatedPoint_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, -500L);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyPendingPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("중복 이벤트 수신 시 DuplicateUserPointHistoryException이 발생하면 멱등 처리하고 acknowledge한다")
    void handleOrderPaymentCompletedEvent_duplicateEvent_idempotentAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, 1500L);
        doThrow(new DuplicateUserPointHistoryException(100L, UserPointSourceType.ORDER, 1L, "상품 구매 적립"))
                .when(modifyPendingPointUseCase).modifyPending(any(ModifyPendingPointCommand.class));

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(modifyPendingPointUseCase).modifyPending(any(ModifyPendingPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 acknowledge하지 않고 예외를 전파한다")
    void handleOrderPaymentCompletedEvent_unexpectedException_propagatesWithoutAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 50000L, 1500L);
        doThrow(new RuntimeException("DB 연결 실패"))
                .when(modifyPendingPointUseCase).modifyPending(any(ModifyPendingPointCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderPaymentCompletedEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 실패");

        verify(modifyPendingPointUseCase).modifyPending(any(ModifyPendingPointCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }
}
