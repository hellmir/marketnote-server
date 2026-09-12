package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventRegisteredEvent;
import com.personal.marketnote.notification.port.in.command.SendBatchNotificationCommand;
import com.personal.marketnote.notification.port.in.usecase.notification.SendBatchNotificationUseCase;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
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
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventRegisteredNotificationConsumerTest {

    @InjectMocks
    private EventRegisteredNotificationConsumer consumer;

    @Mock
    private SendBatchNotificationUseCase sendBatchNotificationUseCase;

    @Mock
    private FindNotificationPreferencePort findNotificationPreferencePort;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long postId, String title) {
        EventRegisteredEvent event = new EventRegisteredEvent(postId, title);
        EventEnvelope<EventRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "community.event.registered", "community-service",
                LocalDateTime.of(2026, 4, 10, 10, 0), event
        );
        return new ConsumerRecord<>("community.event.registered", 0, 0L, "key-1", envelope);
    }

    @Test
    @DisplayName("이벤트 등록 이벤트 수신 시 전체 사용자에게 PUSH_AND_IN_APP 배치 알림을 발송하고 acknowledge한다")
    void shouldSendBatchNotificationWithPushAndInApp() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, "봄맞이 할인 이벤트");
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(List.of(1L, 2L, 3L));

        // when
        consumer.handleEventRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendBatchNotificationCommand> captor = ArgumentCaptor.forClass(SendBatchNotificationCommand.class);
        verify(sendBatchNotificationUseCase).sendBatchNotification(captor.capture());

        SendBatchNotificationCommand command = captor.getValue();
        assertThat(command.userIds()).containsExactly(1L, 2L, 3L);
        assertThat(command.templateCode()).isEqualTo("EVENT_REGISTERED");
        assertThat(command.deliveryChannel()).isEqualTo("PUSH_AND_IN_APP");
        assertThat(command.variables()).containsEntry("post_id", "100");
        assertThat(command.variables()).containsEntry("event_title", "봄맞이 할인 이벤트");
        assertThat(command.scheduledAt()).isNull();

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("사용자 수가 10,000명을 초과하면 청크 분할하여 배치 발송한다")
    void shouldChunkBatchWhenUsersExceedMaxBatchSize() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, "대규모 이벤트");
        List<Long> largeUserIds = LongStream.rangeClosed(1, 15_000).boxed().toList();
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(largeUserIds);

        // when
        consumer.handleEventRegisteredEvent(record, acknowledgment);

        // then
        verify(sendBatchNotificationUseCase, times(2)).sendBatchNotification(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEnvelopeIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "community.event.registered", 0, 0L, "1", null
        );

        // when
        consumer.handleEventRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendBatchNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEventTypeMismatch() {
        // given
        EventRegisteredEvent event = new EventRegisteredEvent(100L, "이벤트");
        EventEnvelope<EventRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "community-service",
                LocalDateTime.of(2026, 4, 10, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "community.event.registered", 0, 0L, "key-1", envelope
        );

        // when
        consumer.handleEventRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendBatchNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("postId가 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenPostIdIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, "이벤트");

        // when
        consumer.handleEventRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendBatchNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("title이 null이면 알림을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenTitleIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, null);

        // when
        consumer.handleEventRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendBatchNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("배치 발송 중 예외가 발생해도 acknowledge한다")
    void shouldAcknowledgeEvenWhenBatchSendFails() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, "이벤트");
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(List.of(1L));
        doThrow(new RuntimeException("발송 실패"))
                .when(sendBatchNotificationUseCase).sendBatchNotification(any());

        // when
        consumer.handleEventRegisteredEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("사용자가 0명이면 배치 발송을 호출하지 않고 acknowledge한다")
    void shouldSkipBatchSendWhenNoUsers() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, "이벤트");
        when(findNotificationPreferencePort.findAllDistinctUserIds()).thenReturn(List.of());

        // when
        consumer.handleEventRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendBatchNotificationUseCase);
        verify(acknowledgment).acknowledge();
    }
}
