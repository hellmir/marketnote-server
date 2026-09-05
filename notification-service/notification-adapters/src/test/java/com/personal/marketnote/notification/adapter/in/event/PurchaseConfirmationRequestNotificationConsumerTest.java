package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ShippingStatusChangedEvent;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseConfirmationRequestNotificationConsumerTest {

    @InjectMocks
    private PurchaseConfirmationRequestNotificationConsumer consumer;

    @Mock
    private SendNotificationUseCase sendNotificationUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(
            Long orderId, Long buyerId, String shippingStatus) {
        ShippingStatusChangedEvent event = new ShippingStatusChangedEvent(
                orderId, buyerId, shippingStatus, "TRACK-001", "CJ",
                LocalDateTime.of(2026, 4, 10, 15, 0)
        );
        EventEnvelope<ShippingStatusChangedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "fulfillment.shipping.status-changed", "fulfillment-service",
                LocalDateTime.of(2026, 4, 10, 15, 0), event
        );
        return new ConsumerRecord<>("fulfillment.shipping.status-changed", 0, 0L,
                String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("배송 상태 DELIVERED 이벤트 수신 시 구매 확정 요청 알림을 예약 발송하고 acknowledge한다")
    void shouldSendScheduledNotificationAndAcknowledgeWhenDeliveredStatus() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 1L, "DELIVERED");

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendNotificationCommand> captor = ArgumentCaptor.forClass(SendNotificationCommand.class);
        verify(sendNotificationUseCase).sendNotification(captor.capture());

        SendNotificationCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(1L);
        assertThat(command.templateCode()).isEqualTo("PURCHASE_CONFIRMATION_REQUEST");
        assertThat(command.deliveryChannel()).isEqualTo("PUSH_AND_IN_APP");
        assertThat(command.variables()).containsEntry("order_id", "100");
        assertThat(command.scheduledAt()).isEqualTo(LocalDateTime.of(2026, 4, 11, 15, 0));

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("배송 상태 SHIPPING 이벤트 수신 시 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenShippingStatus() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 1L, "SHIPPING");

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("배송 상태 PREPARING 이벤트 수신 시 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenPreparingStatus() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 1L, "PREPARING");

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEnvelopeIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "fulfillment.shipping.status-changed", 0, 0L, "1", null
        );

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEventTypeMismatch() {
        // given
        ShippingStatusChangedEvent event = new ShippingStatusChangedEvent(
                100L, 1L, "DELIVERED", "TRACK-001", "CJ",
                LocalDateTime.of(2026, 4, 10, 15, 0)
        );
        EventEnvelope<ShippingStatusChangedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "fulfillment-service",
                LocalDateTime.of(2026, 4, 10, 15, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "fulfillment.shipping.status-changed", 0, 0L, "100", envelope
        );

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenBuyerIdIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, null, "DELIVERED");

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenOrderIdIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 1L, "DELIVERED");

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("occurredAt이 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenOccurredAtIsNull() {
        // given
        ShippingStatusChangedEvent event = new ShippingStatusChangedEvent(
                100L, 1L, "DELIVERED", "TRACK-001", "CJ", null
        );
        EventEnvelope<ShippingStatusChangedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "fulfillment.shipping.status-changed", "fulfillment-service",
                LocalDateTime.of(2026, 4, 10, 15, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "fulfillment.shipping.status-changed", 0, 0L, "100", envelope
        );

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("알림 발송 중 예외가 발생해도 acknowledge한다")
    void shouldAcknowledgeWhenSendNotificationFails() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 1L, "DELIVERED");
        doThrow(new RuntimeException("FCM 발송 실패"))
                .when(sendNotificationUseCase).sendNotification(any(SendNotificationCommand.class));

        // when
        consumer.handleShippingStatusChangedEvent(record, acknowledgment);

        // then
        verify(sendNotificationUseCase).sendNotification(any(SendNotificationCommand.class));
        verify(acknowledgment).acknowledge();
    }
}
