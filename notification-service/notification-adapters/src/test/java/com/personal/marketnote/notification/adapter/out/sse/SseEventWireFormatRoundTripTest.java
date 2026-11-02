package com.personal.marketnote.notification.adapter.out.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.port.out.sse.NotificationSseEventType;
import com.personal.marketnote.notification.port.out.sse.payload.UnreadCountChangedPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static com.personal.marketnote.notification.configuration.RedisPubSubConfig.SSE_NOTIFICATION_EVENTS_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SseEventWireFormatRoundTripTest {

    @Test
    @DisplayName("publish로 발행한 메시지를 역직렬화하면 payload의 필드가 보존된다")
    void shouldPreservePayloadFieldsAfterRoundTrip() throws Exception {
        // given
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();
        RedisSseEventPublishAdapter adapter =
                new RedisSseEventPublishAdapter(stringRedisTemplate, objectMapper);
        UnreadCountChangedPayload payload = UnreadCountChangedPayload.of(9L);

        // when
        adapter.publish(42L, NotificationSseEventType.UNREAD_COUNT_CHANGED, payload);

        // then
        ArgumentCaptor<String> wireCaptor = ArgumentCaptor.forClass(String.class);
        verify(stringRedisTemplate)
                .convertAndSend(eq(SSE_NOTIFICATION_EVENTS_CHANNEL), wireCaptor.capture());
        String wire = wireCaptor.getValue();

        SseEventMessage restored = objectMapper.readValue(wire, SseEventMessage.class);
        assertThat(restored.userId()).isEqualTo(42L);
        assertThat(restored.eventType()).isEqualTo(NotificationSseEventType.UNREAD_COUNT_CHANGED.name());
        assertThat(restored.payload()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> payloadMap = (Map<String, Object>) restored.payload();
        assertThat(payloadMap).containsEntry("unreadCount", 9);
    }
}
