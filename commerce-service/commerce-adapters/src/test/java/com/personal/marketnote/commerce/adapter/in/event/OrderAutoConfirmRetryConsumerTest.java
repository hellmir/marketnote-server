package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderAutoConfirmFailedEvent;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderAutoConfirmRetryConsumer 테스트")
class OrderAutoConfirmRetryConsumerTest {

    @InjectMocks
    private OrderAutoConfirmRetryConsumer consumer;

    @Mock
    private ChangeOrderStatusUseCase changeOrderStatusUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, Long buyerId) {
        OrderAutoConfirmFailedEvent event = new OrderAutoConfirmFailedEvent(
                orderId, buyerId, "DB 오류", LocalDateTime.of(2026, 4, 19, 19, 0)
        );
        EventEnvelope<OrderAutoConfirmFailedEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.ORDER_AUTO_CONFIRM_FAILED,
                "commerce-service",
                LocalDateTime.of(2026, 4, 19, 19, 0),
                event
        );
        return new ConsumerRecord<>(
                KafkaTopicConstants.ORDER_AUTO_CONFIRM_FAILED, 0, 0L, String.valueOf(orderId), envelope
        );
    }

    @Test
    @DisplayName("자동 확정 실패 이벤트 수신 시 CONFIRMED 상태 변경을 재시도하고 acknowledge한다")
    void handleEvent_success_retriesConfirmAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 50L);

        // when
        consumer.handleOrderAutoConfirmFailedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ChangeOrderStatusCommand> captor = ArgumentCaptor.forClass(ChangeOrderStatusCommand.class);
        verify(changeOrderStatusUseCase).changeOrderStatus(captor.capture());

        ChangeOrderStatusCommand command = captor.getValue();
        assertThat(command.id()).isEqualTo(100L);
        assertThat(command.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.ORDER_AUTO_CONFIRM_FAILED, 0, 0L, "1", null
        );

        // when
        consumer.handleOrderAutoConfirmFailedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        OrderAutoConfirmFailedEvent event = new OrderAutoConfirmFailedEvent(
                1L, 50L, "오류", LocalDateTime.of(2026, 4, 19, 19, 0)
        );
        EventEnvelope<OrderAutoConfirmFailedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 4, 19, 19, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.ORDER_AUTO_CONFIRM_FAILED, 0, 0L, "1", envelope
        );

        // when
        consumer.handleOrderAutoConfirmFailedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 50L);

        // when
        consumer.handleOrderAutoConfirmFailedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, 50L);

        // when
        consumer.handleOrderAutoConfirmFailedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(changeOrderStatusUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 CONFIRMED 상태면 OrderStatusAlreadyChangedException은 삼키고 acknowledge한다")
    void handleEvent_alreadyChanged_acknowledgesGracefully() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 50L);
        doThrow(new OrderStatusAlreadyChangedException(OrderStatus.CONFIRMED))
                .when(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));

        // when
        consumer.handleOrderAutoConfirmFailedEvent(record, acknowledgment);

        // then
        verify(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파되고 ack되지 않는다")
    void handleEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 50L);
        doThrow(new RuntimeException("DB 연결 오류"))
                .when(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handleOrderAutoConfirmFailedEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 오류");

        verify(changeOrderStatusUseCase).changeOrderStatus(any(ChangeOrderStatusCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }
}
