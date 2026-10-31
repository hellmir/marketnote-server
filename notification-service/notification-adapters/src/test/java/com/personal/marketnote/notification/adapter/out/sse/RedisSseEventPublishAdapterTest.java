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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisSseEventPublishAdapterTest {

    @InjectMocks
    private RedisSseEventPublishAdapter redisSseEventPublishAdapter;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("publish 호출 시 payload를 JSON으로 직렬화한 뒤 SseEventMessage를 Redis 채널에 발행한다")
    void shouldPublishSerializedMessageToRedisChannel() throws JsonProcessingException {
        // given
        Long userId = 1L;
        NotificationSseEventType eventType = NotificationSseEventType.UNREAD_COUNT_CHANGED;
        UnreadCountChangedPayload payload = UnreadCountChangedPayload.of(5L);
        String payloadJson = "{\"unreadCount\":5}";
        String serialized = "{\"userId\":1,\"eventType\":\"UNREAD_COUNT_CHANGED\",\"payload\":\"{\\\"unreadCount\\\":5}\"}";

        when(objectMapper.writeValueAsString(payload)).thenReturn(payloadJson);
        ArgumentCaptor<SseEventMessage> messageCaptor = ArgumentCaptor.forClass(SseEventMessage.class);
        when(objectMapper.writeValueAsString(messageCaptor.capture())).thenReturn(serialized);

        // when
        redisSseEventPublishAdapter.publish(userId, eventType, payload);

        // then
        SseEventMessage captured = messageCaptor.getValue();
        assertThat(captured.userId()).isEqualTo(userId);
        assertThat(captured.eventType()).isEqualTo("UNREAD_COUNT_CHANGED");
        assertThat(captured.payload()).isEqualTo(payloadJson);
        verify(stringRedisTemplate).convertAndSend(eq(SSE_NOTIFICATION_EVENTS_CHANNEL), eq(serialized));
    }

    @Test
    @DisplayName("payload 직렬화 실패 시 Redis 발행을 수행하지 않는다")
    void shouldNotPublishWhenPayloadSerializationFails() throws JsonProcessingException {
        // given
        UnreadCountChangedPayload payload = UnreadCountChangedPayload.of(5L);
        when(objectMapper.writeValueAsString(payload))
                .thenThrow(new JsonProcessingException("payload serialization error") {
                });

        // when
        redisSseEventPublishAdapter.publish(1L, NotificationSseEventType.UNREAD_COUNT_CHANGED, payload);

        // then
        verify(stringRedisTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    @DisplayName("메시지 직렬화 실패 시 Redis 발행을 수행하지 않는다")
    void shouldNotPublishWhenMessageSerializationFails() throws JsonProcessingException {
        // given
        UnreadCountChangedPayload payload = UnreadCountChangedPayload.of(5L);
        when(objectMapper.writeValueAsString(payload)).thenReturn("{\"unreadCount\":5}");
        when(objectMapper.writeValueAsString(any(SseEventMessage.class)))
                .thenThrow(new JsonProcessingException("message serialization error") {
                });

        // when
        redisSseEventPublishAdapter.publish(1L, NotificationSseEventType.UNREAD_COUNT_CHANGED, payload);

        // then
        verify(stringRedisTemplate, never()).convertAndSend(anyString(), anyString());
    }
}
