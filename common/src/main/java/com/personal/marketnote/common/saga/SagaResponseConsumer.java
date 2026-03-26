package com.personal.marketnote.common.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SagaResponseConsumer {

    private final SagaOrchestrator sagaOrchestrator;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_RESPONSE,
            groupId = "saga-orchestrator"
    )
    public void handleSagaResponse(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.SAGA_RESPONSE)) {
            acknowledgment.acknowledge();
            return;
        }

        SagaResponseMessage responseMessage = envelope.getPayloadAs(SagaResponseMessage.class, objectMapper);

        log.info("SAGA 응답 수신. eventId={}, sagaId={}, sagaType={}, stepName={}, messageType={}, success={}",
                envelope.eventId(), responseMessage.sagaId(), responseMessage.sagaType(),
                responseMessage.stepName(), responseMessage.messageType(), responseMessage.success());

        if (responseMessage.isAction()) {
            sagaOrchestrator.handleStepResponse(
                    responseMessage.sagaId(), responseMessage.stepName(),
                    responseMessage.success(), responseMessage.response());
            acknowledgment.acknowledge();
            return;
        }

        if (responseMessage.isCompensation()) {
            sagaOrchestrator.handleCompensationResponse(
                    responseMessage.sagaId(), responseMessage.stepName(),
                    responseMessage.success(), responseMessage.response());
            acknowledgment.acknowledge();
            return;
        }

        log.warn("알 수 없는 messageType. eventId={}, sagaId={}, messageType={}",
                envelope.eventId(), responseMessage.sagaId(), responseMessage.messageType());
        acknowledgment.acknowledge();
    }
}
