package com.personal.marketnote.notification.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.PushNotificationSentEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationSentEventOutboxAdapter 테스트")
class NotificationSentEventOutboxAdapterTest {

    @InjectMocks
    private NotificationSentEventOutboxAdapter adapter;

    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Spy
    private Clock clock = Clock.fixed(
            Instant.parse("2026-09-03T05:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Test
    @DisplayName("PushNotificationSentEvent를 OutboxEvent로 저장한다")
    void publishSavesOutboxEvent() {
        // given
        PushNotificationSentEvent event = new PushNotificationSentEvent(
                100L, 1L, "ORDER_PAYMENT_COMPLETED", "SENT",
                2, 0, LocalDateTime.of(2026, 4, 10, 14, 0)
        );

        // when
        adapter.publish(event);

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(captor.capture());

        OutboxEvent outboxEvent = captor.getValue();
        assertThat(outboxEvent.getTopic()).isEqualTo(KafkaTopicConstants.NOTIFICATION_PUSH_SENT);
        assertThat(outboxEvent.getPartitionKey()).isEqualTo("100");
        assertThat(outboxEvent.getEventType()).isEqualTo("PushNotificationSentEvent");
        assertThat(outboxEvent.getSource()).isEqualTo("notification-service");
        assertThat(outboxEvent.getEventId()).isNotBlank();
        assertThat(outboxEvent.getPayload()).contains("\"notificationId\":100");
        assertThat(outboxEvent.getPayload()).contains("\"sendStatus\":\"SENT\"");
    }

    @Test
    @DisplayName("페이로드 JSON이 올바르게 직렬화된다")
    void payloadIsValidJson() throws Exception {
        // given
        PushNotificationSentEvent event = new PushNotificationSentEvent(
                200L, 2L, "SHIPPING_STARTED", "SENT",
                1, 1, LocalDateTime.of(2026, 4, 10, 15, 0)
        );

        // when
        adapter.publish(event);

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(captor.capture());

        JsonNode parsed = objectMapper.readTree(captor.getValue().getPayload());
        assertThat(parsed.get("notificationId").asLong()).isEqualTo(200L);
        assertThat(parsed.get("userId").asLong()).isEqualTo(2L);
        assertThat(parsed.get("notificationType").asText()).isEqualTo("SHIPPING_STARTED");
        assertThat(parsed.get("sendStatus").asText()).isEqualTo("SENT");
        assertThat(parsed.get("sentDeviceCount").asInt()).isEqualTo(1);
        assertThat(parsed.get("failedDeviceCount").asInt()).isEqualTo(1);
    }

    @Test
    @DisplayName("직렬화 실패 시 NotificationSentEventSerializationException을 던진다")
    void throwsOnSerializationFailure() throws Exception {
        // given
        PushNotificationSentEvent event = new PushNotificationSentEvent(
                300L, 3L, "NOTICE", "SENT",
                1, 0, LocalDateTime.of(2026, 4, 10, 16, 0)
        );
        when(objectMapper.writeValueAsString(event))
                .thenThrow(new JsonProcessingException("test failure") {});

        // when & then
        assertThatThrownBy(() -> adapter.publish(event))
                .isInstanceOf(NotificationSentEventSerializationException.class)
                .hasMessageContaining("notificationId=300");
    }
}
