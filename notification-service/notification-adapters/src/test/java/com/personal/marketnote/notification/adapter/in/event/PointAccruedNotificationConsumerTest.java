package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PointAccruedEvent;
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
class PointAccruedNotificationConsumerTest {

    @InjectMocks
    private PointAccruedNotificationConsumer consumer;

    @Mock
    private SendNotificationUseCase sendNotificationUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long userId, Long amount) {
        PointAccruedEvent event = new PointAccruedEvent(userId, amount);
        EventEnvelope<PointAccruedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "reward.point.accrued", "reward-service",
                LocalDateTime.of(2026, 4, 10, 10, 0), event
        );
        return new ConsumerRecord<>("reward.point.accrued", 0, 0L, "key-1", envelope);
    }

    @Test
    @DisplayName("포인트 적립 이벤트 수신 시 SendNotificationUseCase를 호출하고 acknowledge한다")
    void shouldSendNotificationAndAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 5000L);

        // when
        consumer.handlePointAccruedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendNotificationCommand> captor = ArgumentCaptor.forClass(SendNotificationCommand.class);
        verify(sendNotificationUseCase).sendNotification(captor.capture());

        SendNotificationCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(1L);
        assertThat(command.templateCode()).isEqualTo("POINT_ACCRUAL");
        assertThat(command.deliveryChannel()).isEqualTo("PUSH_ONLY");
        assertThat(command.variables()).containsEntry("amount", "5000");
        assertThat(command.scheduledAt()).isNull();

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEnvelopeIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "reward.point.accrued", 0, 0L, "1", null
        );

        // when
        consumer.handlePointAccruedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEventTypeMismatch() {
        // given
        PointAccruedEvent event = new PointAccruedEvent(1L, 5000L);
        EventEnvelope<PointAccruedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "reward-service",
                LocalDateTime.of(2026, 4, 10, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "reward.point.accrued", 0, 0L, "key-1", envelope
        );

        // when
        consumer.handlePointAccruedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("userId가 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenUserIdIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 5000L);

        // when
        consumer.handlePointAccruedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("amount가 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenAmountIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null);

        // when
        consumer.handlePointAccruedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }
}
