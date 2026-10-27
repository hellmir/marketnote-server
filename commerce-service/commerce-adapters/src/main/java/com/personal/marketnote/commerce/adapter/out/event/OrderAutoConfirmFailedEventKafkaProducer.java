package com.personal.marketnote.commerce.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.event.exception.OrderEventSerializationException;
import com.personal.marketnote.commerce.port.out.event.PublishOrderAutoConfirmFailedEventPort;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderAutoConfirmFailedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class OrderAutoConfirmFailedEventKafkaProducer implements PublishOrderAutoConfirmFailedEventPort {
    private static final String SOURCE = "commerce-service";
    private static final int MAX_REASON_LENGTH = 1000;

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publish(Long orderId, Long buyerId, String reason, LocalDateTime failedAt) {
        OrderAutoConfirmFailedEvent payload = new OrderAutoConfirmFailedEvent(
                orderId, buyerId, truncate(reason), failedAt
        );
        String topic = KafkaTopicConstants.ORDER_AUTO_CONFIRM_FAILED;
        EventEnvelope<OrderAutoConfirmFailedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        String payloadJson = serialize(envelope, topic);
        OutboxEvent outboxEvent = OutboxEvent.of(
                envelope.eventId(), topic, String.valueOf(orderId),
                envelope.eventType(), SOURCE, payloadJson, clock
        );
        saveOutboxEventPort.save(outboxEvent);
        log.info(
                "자동 확정 실패 Outbox 이벤트 저장. topic={}, orderId={}, buyerId={}, eventId={}",
                topic, orderId, buyerId, envelope.eventId()
        );
    }

    private String serialize(EventEnvelope<OrderAutoConfirmFailedEvent> envelope, String topic) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new OrderEventSerializationException(topic, e);
        }
    }

    private static String truncate(String value) {
        // 순수 null 체크 — FormatValidator.hasValue(Object)는 blank 검사가 포함되어 빈 문자열도 null로 취급되므로 부적합
        if (value == null || value.length() <= MAX_REASON_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_REASON_LENGTH);
    }
}
