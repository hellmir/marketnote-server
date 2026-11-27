package com.personal.marketnote.notification.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.PushNotificationSentEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationSentEventOutboxAdapter 테스트")
class NotificationSentEventOutboxAdapterTest {

    @InjectMocks
    private NotificationSentEventOutboxAdapter adapter;

    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Clock clock;

    @Nested
    @DisplayName("publish 성공")
    class PublishSuccess {

        @Test
        @DisplayName("이벤트를 EventEnvelope으로 감싸 직렬화하여 OutboxEvent로 저장한다")
        void shouldSerializeAndSaveOutboxEvent() throws JsonProcessingException {
            // given
            Clock fixedClock = Clock.fixed(Instant.parse("2026-09-03T05:00:00Z"), ZoneId.of("Asia/Seoul"));
            when(clock.instant()).thenReturn(fixedClock.instant());
            when(clock.getZone()).thenReturn(fixedClock.getZone());

            PushNotificationSentEvent event = new PushNotificationSentEvent(
                100L, 1L, "ORDER_PAYMENT_COMPLETED", "SENT",
                2, 0, LocalDateTime.of(2026, 4, 10, 14, 0)
            );
            ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
            when(objectMapper.writeValueAsString(envelopeCaptor.capture()))
                .thenReturn("{\"eventType\":\"notification.push.sent\"}");

            // when
            adapter.publish(event);

            // then
            EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
            assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.NOTIFICATION_PUSH_SENT);
            assertThat(capturedEnvelope.source()).isEqualTo("notification-service");
            assertThat(capturedEnvelope.eventId()).isNotBlank();
            assertThat(capturedEnvelope.payload()).isEqualTo(event);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(saveOutboxEventPort).save(captor.capture());

            OutboxEvent savedEvent = captor.getValue();
            assertThat(savedEvent.getTopic()).isEqualTo(KafkaTopicConstants.NOTIFICATION_PUSH_SENT);
            assertThat(savedEvent.getPartitionKey()).isEqualTo("100");
            assertThat(savedEvent.getEventType()).isEqualTo("PushNotificationSentEvent");
            assertThat(savedEvent.getSource()).isEqualTo("notification-service");
            assertThat(savedEvent.getEventId()).isNotBlank();
            assertThat(savedEvent.getEventId()).isEqualTo(capturedEnvelope.eventId());
            assertThat(savedEvent.getPayload()).isEqualTo("{\"eventType\":\"notification.push.sent\"}");
        }
    }

    @Nested
    @DisplayName("publish 실패")
    class PublishFailure {

        @Test
        @DisplayName("직렬화 실패 시 NotificationSentEventSerializationException이 발생한다")
        void shouldThrowSerializationExceptionWhenSerializationFails() throws JsonProcessingException {
            // given
            Clock fixedClock = Clock.fixed(Instant.parse("2026-09-03T05:00:00Z"), ZoneId.of("Asia/Seoul"));
            when(clock.instant()).thenReturn(fixedClock.instant());
            when(clock.getZone()).thenReturn(fixedClock.getZone());

            PushNotificationSentEvent event = new PushNotificationSentEvent(
                300L, 3L, "NOTICE", "SENT",
                1, 0, LocalDateTime.of(2026, 4, 10, 16, 0)
            );
            when(objectMapper.writeValueAsString(any(EventEnvelope.class)))
                .thenThrow(new JsonProcessingException("test failure") {
                });

            // when & then
            assertThatThrownBy(() -> adapter.publish(event))
                .isInstanceOf(NotificationSentEventSerializationException.class)
                .hasMessageContaining("ERR_NOTIFICATION_SENT_EVENT_01")
                .hasMessageContaining("알림 발송 완료 이벤트")
                .hasMessageContaining("topic=" + KafkaTopicConstants.NOTIFICATION_PUSH_SENT)
                .hasMessageContaining("partitionKey=300")
                .hasMessageContaining("eventId=");
            verifyNoInteractions(saveOutboxEventPort);
        }
    }
}
