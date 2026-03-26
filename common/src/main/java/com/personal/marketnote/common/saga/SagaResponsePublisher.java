package com.personal.marketnote.common.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.common.saga.exception.SagaSerializationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SagaResponsePublisher {

    private static final String SOURCE = "saga-step-handler";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional(isolation = READ_COMMITTED)
    public void publishSuccess(String sagaId, String sagaType, String stepName,
                                String messageType, String response) {
        SagaResponseMessage responseMessage = new SagaResponseMessage(
                sagaId, sagaType, stepName, messageType, true, response);
        publishToOutbox(responseMessage, sagaId);
    }

    @Transactional(isolation = READ_COMMITTED)
    public void publishFailure(String sagaId, String sagaType, String stepName,
                                String messageType, String errorMessage) {
        SagaResponseMessage responseMessage = new SagaResponseMessage(
                sagaId, sagaType, stepName, messageType, false, errorMessage);
        publishToOutbox(responseMessage, sagaId);
    }

    private void publishToOutbox(SagaResponseMessage message, String partitionKey) {
        EventEnvelope<SagaResponseMessage> envelope = EventEnvelope.of(
                KafkaTopicConstants.SAGA_RESPONSE, SOURCE, message, clock);
        String envelopeJson = serialize(envelope);
        OutboxEvent event = OutboxEvent.of(
                envelope.eventId(), KafkaTopicConstants.SAGA_RESPONSE, partitionKey,
                envelope.eventType(), SOURCE, envelopeJson, clock);
        saveOutboxEventPort.save(event);
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new SagaSerializationException("SAGA 응답 직렬화에 실패했습니다.", e);
        }
    }
}
