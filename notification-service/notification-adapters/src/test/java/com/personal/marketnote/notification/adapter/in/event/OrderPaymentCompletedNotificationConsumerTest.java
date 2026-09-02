package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.notification.port.in.command.SendNotificationCommand;
import com.personal.marketnote.notification.port.in.usecase.notification.SendNotificationUseCase;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPaymentCompletedNotificationConsumerTest {

    @InjectMocks
    private OrderPaymentCompletedNotificationConsumer consumer;

    @Mock
    private SendNotificationUseCase sendNotificationUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId, Long buyerId) {
        OrderPaymentCompletedEvent event = new OrderPaymentCompletedEvent(
                orderId, buyerId, 50000L, 0L, List.of(), 0L
        );
        EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.payment-completed", "commerce-service",
                LocalDateTime.of(2026, 4, 10, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.payment-completed", 0, 0L, "key-1", envelope);
    }

    @Test
    @DisplayName("주문 결제 완료 이벤트 수신 시 SendNotificationUseCase를 호출하고 acknowledge한다")
    void shouldSendNotificationAndAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 1L);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendNotificationCommand> captor = ArgumentCaptor.forClass(SendNotificationCommand.class);
        verify(sendNotificationUseCase).sendNotification(captor.capture());

        SendNotificationCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(1L);
        assertThat(command.templateCode()).isEqualTo("ORDER_PAYMENT_COMPLETED");
        assertThat(command.deliveryChannel()).isEqualTo("PUSH_ONLY");
        assertThat(command.variables()).containsEntry("order_id", "100");
        assertThat(command.scheduledAt()).isNull();

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEnvelopeIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.payment-completed", 0, 0L, "1", null
        );

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEventTypeMismatch() {
        // given
        OrderPaymentCompletedEvent event = new OrderPaymentCompletedEvent(
                100L, 1L, 50000L, 0L, List.of(), 0L
        );
        EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 4, 10, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.payment-completed", 0, 0L, "key-1", envelope
        );

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenBuyerIdIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, null);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenOrderIdIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 1L);

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }
}
