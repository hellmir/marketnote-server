package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.FulfillmentCancelSagaPayload;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleasePort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleaseResult;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.common.utility.FormatValidator;
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
public class OrderCancelFulfillmentSagaConsumer {

    private static final String REQUIRES_FULFILLMENT_CANCEL = "PREPARING";

    private final ObjectMapper objectMapper;
    private final CancelFulfillmentReleasePort cancelFulfillmentReleasePort;
    private final SagaResponsePublisher sagaResponsePublisher;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_CANCEL_FULFILLMENT,
            groupId = "saga-order-cancel-fulfillment"
    )
    public void handleFulfillmentCancelStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

        log.info("SAGA 풀필먼트 취소 스텝 수신. sagaId={}, messageType={}",
                stepMessage.sagaId(), stepMessage.messageType());

        if (SagaStepMessage.ACTION.equals(stepMessage.messageType())) {
            handleAction(stepMessage);
            acknowledgment.acknowledge();
            return;
        }

        log.warn("풀필먼트 취소 스텝은 보상이 없음. 알 수 없는 messageType. sagaId={}, messageType={}",
                stepMessage.sagaId(), stepMessage.messageType());
        acknowledgment.acknowledge();
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            FulfillmentCancelSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), FulfillmentCancelSagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }

            if (!REQUIRES_FULFILLMENT_CANCEL.equals(payload.originalStatus())) {
                log.info("풀필먼트 취소 불필요 (originalStatus={}). sagaId={}, orderId={}",
                        payload.originalStatus(), stepMessage.sagaId(), payload.orderId());
                sagaResponsePublisher.publishSuccess(
                        stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                        stepMessage.messageType(), "{\"success\":true,\"skipped\":true}");
                return;
            }

            CancelFulfillmentReleaseResult result = cancelFulfillmentReleasePort.cancelRelease(payload.orderId());

            if (!result.cancelled()) {
                log.warn("풀필먼트 출고 취소 거부. sagaId={}, orderId={}, message={}",
                        stepMessage.sagaId(), payload.orderId(), result.message());
                sagaResponsePublisher.publishFailure(
                        stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                        stepMessage.messageType(), "풀필먼트 출고 취소 거부: " + result.message());
                return;
            }

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}");

            log.info("SAGA 풀필먼트 출고 취소 성공. sagaId={}, orderId={}",
                    stepMessage.sagaId(), payload.orderId());
        } catch (Exception e) {
            log.error("SAGA 풀필먼트 출고 취소 처리 실패. sagaId={}, error={}",
                    stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "풀필먼트 출고 취소 처리 실패");
        }
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 풀필먼트 취소 스텝 페이로드 검증 실패. sagaId={}, reason={}",
                stepMessage.sagaId(), reason);
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason);
    }
}
