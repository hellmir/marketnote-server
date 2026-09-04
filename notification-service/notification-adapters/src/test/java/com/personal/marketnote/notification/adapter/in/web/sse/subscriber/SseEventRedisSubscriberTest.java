package com.personal.marketnote.notification.adapter.in.web.sse.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.adapter.in.web.sse.registry.SseConnectionRegistry;
import com.personal.marketnote.notification.adapter.out.sse.SseEventMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SseEventRedisSubscriberTest {

    @InjectMocks
    private SseEventRedisSubscriber sseEventRedisSubscriber;

    @Mock
    private SseConnectionRegistry sseConnectionRegistry;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Redis 메시지 수신 시 SseConnectionRegistry에 이벤트를 전달한다")
    void shouldDeliverEventToSseConnectionRegistry() throws Exception {
        // given
        String messageBody = "{\"userId\":1,\"eventType\":\"UNREAD_COUNT_CHANGED\",\"payload\":\"{\\\"unreadCount\\\":5}\"}";
        Message message = new DefaultMessage("sse:notification-events".getBytes(), messageBody.getBytes());
        SseEventMessage sseEventMessage = new SseEventMessage(1L, "UNREAD_COUNT_CHANGED", "{\"unreadCount\":5}");

        when(objectMapper.readValue(messageBody, SseEventMessage.class)).thenReturn(sseEventMessage);

        // when
        sseEventRedisSubscriber.onMessage(message, null);

        // then
        verify(sseConnectionRegistry).sendToUser(eq(1L), any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("역직렬화 실패 시 SseConnectionRegistry에 이벤트를 전달하지 않는다")
    void shouldNotDeliverEventWhenDeserializationFails() throws Exception {
        // given
        String invalidMessage = "invalid-json";
        Message message = new DefaultMessage("sse:notification-events".getBytes(), invalidMessage.getBytes());

        when(objectMapper.readValue(invalidMessage, SseEventMessage.class))
                .thenThrow(new JsonProcessingException("parse error") {});

        // when
        sseEventRedisSubscriber.onMessage(message, null);

        // then
        verifyNoInteractions(sseConnectionRegistry);
    }

    @Test
    @DisplayName("해당 userId의 SSE 커넥션이 없으면 무시한다")
    void shouldIgnoreWhenNoSseConnectionForUser() throws Exception {
        // given
        String messageBody = "{\"userId\":999,\"eventType\":\"UNREAD_COUNT_CHANGED\",\"payload\":\"{}\"}";
        Message message = new DefaultMessage("sse:notification-events".getBytes(), messageBody.getBytes());
        SseEventMessage sseEventMessage = new SseEventMessage(999L, "UNREAD_COUNT_CHANGED", "{}");

        when(objectMapper.readValue(messageBody, SseEventMessage.class)).thenReturn(sseEventMessage);

        // when
        sseEventRedisSubscriber.onMessage(message, null);

        // then - sendToUser 호출은 되지만 내부에서 커넥션 없으면 무시
        verify(sseConnectionRegistry).sendToUser(eq(999L), any(SseEmitter.SseEventBuilder.class));
    }
}
