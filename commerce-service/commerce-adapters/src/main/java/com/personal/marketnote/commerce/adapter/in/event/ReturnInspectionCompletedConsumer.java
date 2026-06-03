package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnCommand;
import com.personal.marketnote.commerce.port.in.usecase.returntracker.CompleteReturnUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ReturnInspectionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnInspectionCompletedConsumer {
    private final ObjectMapper objectMapper;
    private final CompleteReturnUseCase completeReturnUseCase;

    @KafkaListener(
            topics = KafkaTopicConstants.RETURN_INSPECTION_COMPLETED,
            groupId = "commerce-return-inspection-completed"
    )
    public void handleReturnInspectionCompletedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.RETURN_INSPECTION_COMPLETED)) {
            acknowledgment.acknowledge();
            return;
        }

        ReturnInspectionCompletedEvent payload = envelope.getPayloadAs(
                ReturnInspectionCompletedEvent.class, objectMapper
        );

        log.info("반품 검수 완료 이벤트 수신. eventId={}, orderId={}",
                envelope.eventId(), payload.orderId());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()))) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            CompleteReturnCommand command = new CompleteReturnCommand(payload.orderId());
            completeReturnUseCase.completeReturn(command);

            log.info("반품 완료 처리 성공. orderId={}", payload.orderId());
        } catch (Exception e) {
            log.error("반품 완료 처리 실패. orderId={}, error={}",
                    payload.orderId(), e.getMessage(), e);
        }

        acknowledgment.acknowledge();
    }
}
