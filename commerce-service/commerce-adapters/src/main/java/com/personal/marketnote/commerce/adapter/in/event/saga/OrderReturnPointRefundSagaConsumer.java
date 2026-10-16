package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.ReturnPointRefundSagaPayload;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
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
public class OrderReturnPointRefundSagaConsumer {

    private final ObjectMapper objectMapper;
    private final ModifyUserPointPort modifyUserPointPort;
    private final SagaResponsePublisher sagaResponsePublisher;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_RETURN_POINT_REFUND,
            groupId = "saga-order-return-point-refund"
    )
    public void handlePointRefundStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        try {
            EventEnvelope<?> envelope = record.value();

            if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
                return;
            }

            SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

            log.info("SAGA 반품 포인트 환불 스텝 수신. sagaId={}, messageType={}", stepMessage.sagaId(), stepMessage.messageType());

            if (SagaStepMessage.ACTION.equals(stepMessage.messageType())) {
                handleAction(stepMessage);
                return;
            }

            if (SagaStepMessage.COMPENSATION.equals(stepMessage.messageType())) {
                handleCompensation(stepMessage);
                return;
            }

            log.warn("알 수 없는 messageType. sagaId={}, messageType={}", stepMessage.sagaId(), stepMessage.messageType());
        } catch (Exception e) {
            log.error("SAGA 반품 포인트 환불 스텝 처리 실패. topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e
            );
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            ReturnPointRefundSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), ReturnPointRefundSagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId()) || FormatValidator.hasNoValue(payload.buyerId())) {
                publishValidationFailure(stepMessage, "orderId 또는 buyerId 누락");
                return;
            }

            if (FormatValidator.hasNoValue(payload.refundPointAmount()) || payload.refundPointAmount() <= 0) {
                log.info("포인트 미사용 주문 (환불 생략). sagaId={}, orderId={}", stepMessage.sagaId(), payload.orderId());
                sagaResponsePublisher.publishSuccess(
                        stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                        stepMessage.messageType(), "{\"success\":true,\"skipped\":true}"
                );
                return;
            }

            modifyUserPointPort.refundOrderPoints(payload.buyerId(), payload.refundPointAmount(), payload.orderId());

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}"
            );

            log.info("SAGA 반품 포인트 환불 성공. sagaId={}, orderId={}, buyerId={}, refundPointAmount={}",
                    stepMessage.sagaId(), payload.orderId(), payload.buyerId(), payload.refundPointAmount()
            );
        } catch (Exception e) {
            log.error("SAGA 반품 포인트 환불 처리 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "반품 포인트 환불 처리 실패"
            );
        }
    }

    private void handleCompensation(SagaStepMessage stepMessage) {
        try {
            ReturnPointRefundSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), ReturnPointRefundSagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId()) || FormatValidator.hasNoValue(payload.buyerId())) {
                publishValidationFailure(stepMessage, "orderId 또는 buyerId 누락");
                return;
            }

            if (FormatValidator.hasNoValue(payload.refundPointAmount()) || payload.refundPointAmount() <= 0) {
                log.info("포인트 미사용 주문 보상 (생략). sagaId={}, orderId={}", stepMessage.sagaId(), payload.orderId());
                sagaResponsePublisher.publishSuccess(
                        stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                        stepMessage.messageType(), "{\"compensated\":true,\"skipped\":true}"
                );
                return;
            }

            modifyUserPointPort.deductOrderPoints(payload.buyerId(), payload.refundPointAmount(), payload.orderId());

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true}"
            );

            log.info("SAGA 반품 포인트 환불 보상 완료 (재차감). sagaId={}, orderId={}, buyerId={}, amount={}",
                    stepMessage.sagaId(), payload.orderId(), payload.buyerId(), payload.refundPointAmount()
            );
        } catch (Exception e) {
            log.error("SAGA 반품 포인트 환불 보상 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "반품 포인트 환불 보상 처리 실패"
            );
        }
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 반품 포인트 환불 스텝 페이로드 검증 실패. sagaId={}, reason={}", stepMessage.sagaId(), reason);
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason
        );
    }
}
