package com.personal.marketnote.notification.adapter.out.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.port.out.sse.PublishSseEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static com.personal.marketnote.notification.configuration.RedisPubSubConfig.SSE_NOTIFICATION_EVENTS_CHANNEL;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSseEventPublishAdapter implements PublishSseEventPort {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(Long userId, String eventType, String payload) {
        SseEventMessage message = new SseEventMessage(userId, eventType, payload);

        String serialized = serialize(message);
        if (FormatValidator.hasNoValue(serialized)) {
            return;
        }

        stringRedisTemplate.convertAndSend(SSE_NOTIFICATION_EVENTS_CHANNEL, serialized);
        log.debug("SSE 이벤트 Redis 발행: userId={}, eventType={}", userId, eventType);
    }

    private String serialize(SseEventMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException exception) {
            log.error("SSE 이벤트 메시지 직렬화 실패: userId={}, eventType={}",
                    message.userId(), message.eventType(), exception);
            return null;
        }
    }
}
