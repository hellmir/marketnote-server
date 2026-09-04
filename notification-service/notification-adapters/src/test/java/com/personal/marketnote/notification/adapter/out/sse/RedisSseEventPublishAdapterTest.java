package com.personal.marketnote.notification.adapter.out.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static com.personal.marketnote.notification.configuration.RedisPubSubConfig.SSE_NOTIFICATION_EVENTS_CHANNEL;
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
    @DisplayName("publish 호출 시 Redis 채널에 JSON 직렬화된 메시지를 발행한다")
    void shouldPublishSerializedMessageToRedisChannel() throws JsonProcessingException {
        // given
        Long userId = 1L;
        String eventType = "UNREAD_COUNT_CHANGED";
        String payload = "{\"unreadCount\":5}";
        String serialized = "{\"userId\":1,\"eventType\":\"UNREAD_COUNT_CHANGED\",\"payload\":\"{\\\"unreadCount\\\":5}\"}";

        when(objectMapper.writeValueAsString(any(SseEventMessage.class))).thenReturn(serialized);

        // when
        redisSseEventPublishAdapter.publish(userId, eventType, payload);

        // then
        verify(stringRedisTemplate).convertAndSend(eq(SSE_NOTIFICATION_EVENTS_CHANNEL), eq(serialized));
    }

    @Test
    @DisplayName("직렬화 실패 시 Redis 발행을 수행하지 않는다")
    void shouldNotPublishWhenSerializationFails() throws JsonProcessingException {
        // given
        when(objectMapper.writeValueAsString(any(SseEventMessage.class)))
                .thenThrow(new JsonProcessingException("serialization error") {});

        // when
        redisSseEventPublishAdapter.publish(1L, "UNREAD_COUNT_CHANGED", "{}");

        // then
        verify(stringRedisTemplate, never()).convertAndSend(anyString(), anyString());
    }
}
