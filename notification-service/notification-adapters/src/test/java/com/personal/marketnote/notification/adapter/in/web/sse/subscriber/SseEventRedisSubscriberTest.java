package com.personal.marketnote.notification.adapter.in.web.sse.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.adapter.in.web.sse.registry.SseConnectionRegistry;
import com.personal.marketnote.notification.adapter.out.sse.SseEventMessage;
import com.personal.marketnote.notification.port.out.sse.NotificationSseEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SseEventRedisSubscriberTest {

    @InjectMocks
    private SseEventRedisSubscriber sseEventRedisSubscriber;

    @Mock
    private SseConnectionRegistry sseConnectionRegistry;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("payload가 JSON object인 메시지를 수신하면 SseConnectionRegistry에 이벤트를 전달한다")
    void shouldDeliverEventWithJsonObjectPayloadToSseConnectionRegistry() throws Exception {
        // given
        String messageBody = "{\"userId\":1,\"eventType\":\""
                + NotificationSseEventType.UNREAD_COUNT_CHANGED.name()
                + "\",\"payload\":{\"unreadCount\":5}}";
        Message message = new DefaultMessage("sse:notification-events".getBytes(), messageBody.getBytes());
        Map<String, Object> payloadMap = new LinkedHashMap<>();
        payloadMap.put("unreadCount", 5);
        SseEventMessage sseEventMessage = new SseEventMessage(
                1L, NotificationSseEventType.UNREAD_COUNT_CHANGED.name(), payloadMap
        );

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
                .thenThrow(new JsonProcessingException("parse error") {
                });

        // when
        sseEventRedisSubscriber.onMessage(message, null);

        // then
        verifyNoInteractions(sseConnectionRegistry);
    }

    @Test
    @DisplayName("해당 userId의 SSE 커넥션이 없어도 sendToUser는 호출된다 (Registry 내부에서 무시)")
    void shouldInvokeSendToUserEvenWhenNoConnection() throws Exception {
        // given
        String messageBody = "{\"userId\":999,\"eventType\":\""
                + NotificationSseEventType.UNREAD_COUNT_CHANGED.name()
                + "\",\"payload\":{\"unreadCount\":0}}";
        Message message = new DefaultMessage("sse:notification-events".getBytes(), messageBody.getBytes());
        Map<String, Object> payloadMap = new LinkedHashMap<>();
        payloadMap.put("unreadCount", 0);
        SseEventMessage sseEventMessage = new SseEventMessage(
                999L, NotificationSseEventType.UNREAD_COUNT_CHANGED.name(), payloadMap
        );

        when(objectMapper.readValue(messageBody, SseEventMessage.class)).thenReturn(sseEventMessage);

        // when
        sseEventRedisSubscriber.onMessage(message, null);

        // then
        verify(sseConnectionRegistry).sendToUser(eq(999L), any(SseEmitter.SseEventBuilder.class));
    }
}
