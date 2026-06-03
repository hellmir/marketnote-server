package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderReturnedEvent;
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
@DisplayName("OrderReturnedPointRefundConsumer 테스트")
class OrderReturnedPointRefundConsumerTest {
    @InjectMocks
    private OrderReturnedPointRefundConsumer consumer;

    @Mock
    private ModifyUserPointUseCase modifyUserPointUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, Long buyerId,
                                                                 Long pointAmount, boolean isFullReturn) {
        OrderReturnedEvent event = new OrderReturnedEvent(
                orderId, "order-key-1", buyerId, 50000L, 80000L, pointAmount, 3000L,
                isFullReturn, 2500L, List.of()
        );
        EventEnvelope<OrderReturnedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.returned", "commerce-service",
                LocalDateTime.of(2026, 4, 9, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.returned", 0, 0L, String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("전체 반품 이벤트 수신 시 포인트를 환불하고 acknowledge한다")
    void handleOrderReturnedEvent_fullReturn_refundsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 1000L, true);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ModifyUserPointCommand> captor = ArgumentCaptor.forClass(ModifyUserPointCommand.class);
        verify(modifyUserPointUseCase).modify(captor.capture());
        ModifyUserPointCommand command = captor.getValue();

        assertThat(command.userId()).isEqualTo(100L);
        assertThat(command.changeType()).isEqualTo(UserPointChangeType.ACCRUAL);
        assertThat(command.amount()).isEqualTo(1000L);
        assertThat(command.sourceType()).isEqualTo(UserPointSourceType.ORDER);
        assertThat(command.sourceId()).isEqualTo(1L);
        assertThat(command.reason()).isEqualTo("반품 완료 포인트 환불");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("부분 반품 이벤트이면 포인트 환불 없이 acknowledge한다")
    void handleOrderReturnedEvent_partialReturn_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 1000L, false);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 포인트 환불 없이 acknowledge한다")
    void handleOrderReturnedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 100L, 1000L, true);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 null이면 포인트 환불 없이 acknowledge한다")
    void handleOrderReturnedEvent_nullBuyerId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null, 1000L, true);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("pointAmount가 null이면 포인트 환불 없이 acknowledge한다")
    void handleOrderReturnedEvent_nullPointAmount_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, null, true);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("pointAmount가 0이면 포인트 환불 없이 acknowledge한다")
    void handleOrderReturnedEvent_zeroPointAmount_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 0L, true);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("pointAmount가 음수이면 포인트 환불 없이 acknowledge한다")
    void handleOrderReturnedEvent_negativePointAmount_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, -500L, true);

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 포인트 환불 없이 acknowledge한다")
    void handleOrderReturnedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.returned", 0, 0L, "1", null
        );

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleOrderReturnedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        OrderReturnedEvent event = new OrderReturnedEvent(
                1L, "order-key-1", 100L, 50000L, 80000L, 1000L, 3000L,
                true, 2500L, List.of()
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
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("중복 이벤트 수신 시 DuplicateUserPointHistoryException이 발생하면 멱등 처리하고 acknowledge한다")
    void handleOrderReturnedEvent_duplicateEvent_idempotentAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 1000L, true);
        doThrow(new DuplicateUserPointHistoryException(100L, UserPointSourceType.ORDER, 1L, "반품 완료 포인트 환불"))
                .when(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));

        // when
        consumer.handleOrderReturnedEvent(record, acknowledgment);

        // then
        verify(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("UseCase에서 예상치 못한 예외 발생 시 acknowledge하지 않고 예외를 전파한다")
    void handleOrderReturnedEvent_useCaseUnexpectedException_propagatesWithoutAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L, 1000L, true);
        doThrow(new RuntimeException("DB 연결 실패"))
                .when(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderReturnedEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 실패");

        verify(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }
}
