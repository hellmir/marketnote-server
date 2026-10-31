package com.personal.marketnote.notification.adapter.out.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.port.out.sse.NotificationSseEventType;
import com.personal.marketnote.notification.port.out.sse.payload.UnreadCountChangedPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static com.personal.marketnote.notification.configuration.RedisPubSubConfig.SSE_NOTIFICATION_EVENTS_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisSseEventPublishAdapterTest {

    @InjectMocks
    private RedisSseEventPublishAdapter redisSseEventPublishAdapter;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("SseEventPayload를 JSON object로 단일 직렬화하여 Redis 채널에 발행한다")
    void shouldPublishPayloadAsJsonObjectWithSingleSerialization() throws JsonProcessingException {
        // given
        Long userId = 1L;
        NotificationSseEventType eventType = NotificationSseEventType.UNREAD_COUNT_CHANGED;
        UnreadCountChangedPayload payload = UnreadCountChangedPayload.of(5L);
        String serialized = "{\"userId\":1,\"eventType\":\"UNREAD_COUNT_CHANGED\",\"payload\":{\"unreadCount\":5}}";

        ArgumentCaptor<SseEventMessage> messageCaptor = ArgumentCaptor.forClass(SseEventMessage.class);
        when(objectMapper.writeValueAsString(messageCaptor.capture())).thenReturn(serialized);

        // when
        redisSseEventPublishAdapter.publish(userId, eventType, payload);

        // then
        SseEventMessage captured = messageCaptor.getValue();
        assertThat(captured.userId()).isEqualTo(userId);
        assertThat(captured.eventType()).isEqualTo("UNREAD_COUNT_CHANGED");
        assertThat(captured.payload()).isSameAs(payload);
        verify(stringRedisTemplate).convertAndSend(eq(SSE_NOTIFICATION_EVENTS_CHANNEL), eq(serialized));
    }

    @Test
    @DisplayName("직렬화된 메시지에 payload가 JSON object로 포함되고 문자열 이중 인코딩이 없다")
    void shouldNotDoubleEncodePayloadAsString() throws JsonProcessingException {
        // given
        UnreadCountChangedPayload payload = UnreadCountChangedPayload.of(7L);
        ObjectMapper realObjectMapper = new ObjectMapper();
        RedisSseEventPublishAdapter adapter =
                new RedisSseEventPublishAdapter(stringRedisTemplate, realObjectMapper);
        ArgumentCaptor<String> wireCaptor = ArgumentCaptor.forClass(String.class);

        // when
        adapter.publish(2L, NotificationSseEventType.UNREAD_COUNT_CHANGED, payload);

        // then
        verify(stringRedisTemplate)
                .convertAndSend(eq(SSE_NOTIFICATION_EVENTS_CHANNEL), wireCaptor.capture());
        String wire = wireCaptor.getValue();
        assertThat(wire).contains("\"payload\":{\"unreadCount\":7}");
        assertThat(wire).doesNotContain("\"payload\":\"");
    }

    @Test
    @DisplayName("메시지 직렬화 실패 시 Redis 발행을 수행하지 않는다")
    void shouldNotPublishWhenMessageSerializationFails() throws JsonProcessingException {
        // given
        UnreadCountChangedPayload payload = UnreadCountChangedPayload.of(5L);
        when(objectMapper.writeValueAsString(any(SseEventMessage.class)))
                .thenThrow(new JsonProcessingException("message serialization error") {
                });

        // when
        redisSseEventPublishAdapter.publish(1L, NotificationSseEventType.UNREAD_COUNT_CHANGED, payload);

        // then
        verify(stringRedisTemplate, never()).convertAndSend(anyString(), anyString());
    }
}
