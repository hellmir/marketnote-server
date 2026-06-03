package com.personal.marketnote.commerce.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.out.event.PublishReturnTrackerEventPort;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ReturnInspectionCompletedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class ReturnTrackerEventKafkaProducer implements PublishReturnTrackerEventPort {
    private static final String SOURCE = "commerce-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publishReturnInspectionCompletedEvent(Long orderId) {
        ReturnInspectionCompletedEvent payload = new ReturnInspectionCompletedEvent(orderId);
        String topic = KafkaTopicConstants.RETURN_INSPECTION_COMPLETED;
        EventEnvelope<ReturnInspectionCompletedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        saveToOutbox(envelope, topic, orderId.toString());
    }

    private <T> void saveToOutbox(EventEnvelope<T> envelope, String topic, String partitionKey) {
        String payloadJson = serializeEnvelope(envelope);
        OutboxEvent outboxEvent = OutboxEvent.of(
                envelope.eventId(), topic, partitionKey,
                envelope.eventType(), SOURCE, payloadJson, clock
        );
        saveOutboxEventPort.save(outboxEvent);
        log.info("Outbox 이벤트 저장. topic={}, partitionKey={}, eventId={}",
                topic, partitionKey, envelope.eventId());
    }

    private <T> String serializeEnvelope(EventEnvelope<T> envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("이벤트 직렬화 실패: " + e.getMessage(), e);
        }
    }
}
