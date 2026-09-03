package com.personal.marketnote.notification.adapter.in.web.sse.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.adapter.in.web.sse.registry.SseConnectionRegistry;
import com.personal.marketnote.notification.adapter.out.sse.SseEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class SseEventRedisSubscriber implements MessageListener {

    private final SseConnectionRegistry sseConnectionRegistry;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);

        SseEventMessage sseEventMessage = deserialize(messageBody);
        if (FormatValidator.hasNoValue(sseEventMessage)) {
            return;
        }

        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name(sseEventMessage.eventType())
                .data(sseEventMessage.payload());

        sseConnectionRegistry.sendToUser(sseEventMessage.userId(), event);
    }

    private SseEventMessage deserialize(String messageBody) {
        try {
            return objectMapper.readValue(messageBody, SseEventMessage.class);
        } catch (JsonProcessingException exception) {
            log.error("SSE 이벤트 메시지 역직렬화 실패: message={}", messageBody, exception);
            return null;
        }
    }
}
