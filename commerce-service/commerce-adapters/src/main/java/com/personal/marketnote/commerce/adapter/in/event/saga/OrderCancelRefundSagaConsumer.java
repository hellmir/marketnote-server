package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.RefundPaymentSagaPayload;
import com.personal.marketnote.commerce.exception.PaymentAlreadyRefundedException;
import com.personal.marketnote.commerce.port.in.command.payment.RefundPaymentCommand;
import com.personal.marketnote.commerce.port.in.usecase.payment.RefundPaymentUseCase;
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
public class OrderCancelRefundSagaConsumer {

    private final ObjectMapper objectMapper;
    private final RefundPaymentUseCase refundPaymentUseCase;
    private final SagaResponsePublisher sagaResponsePublisher;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_CANCEL_REFUND,
            groupId = "saga-order-cancel-refund"
    )
    public void handleRefundPaymentStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        try {
            EventEnvelope<?> envelope = record.value();

            if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
                return;
            }

            SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

            log.info("SAGA PG 환불 스텝 수신. sagaId={}, messageType={}",
                    stepMessage.sagaId(), stepMessage.messageType());

            if (SagaStepMessage.ACTION.equals(stepMessage.messageType())) {
                handleAction(stepMessage);
                return;
            }

            log.warn("PG 환불 스텝은 보상이 없음. 알 수 없는 messageType. sagaId={}, messageType={}",
                    stepMessage.sagaId(), stepMessage.messageType());
        } catch (Exception e) {
            log.error("SAGA PG 환불 스텝 메시지 처리 실패. topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e);
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            RefundPaymentSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), RefundPaymentSagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }

            if (FormatValidator.hasNoValue(payload.orderKey())) {
                publishValidationFailure(stepMessage, "orderKey 누락");
                return;
            }

            if (FormatValidator.hasNoValue(payload.paymentAmount())) {
                publishValidationFailure(stepMessage, "paymentAmount 누락");
                return;
            }

            if (!payload.isFullCancel() && FormatValidator.hasNoValue(payload.cancelAmount())) {
                publishValidationFailure(stepMessage, "부분 취소 시 cancelAmount 누락");
                return;
            }

            RefundPaymentCommand command = RefundPaymentCommand.builder()
                    .orderKey(payload.orderKey())
                    .orderId(payload.orderId())
                    .cancelAmount(payload.cancelAmount())
                    .paymentAmount(payload.paymentAmount())
                    .isFullCancel(payload.isFullCancel())
                    .alreadyRefunded(payload.alreadyRefunded())
                    .build();

            refundPaymentUseCase.refund(command);

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}");

            log.info("SAGA PG 환불 성공. sagaId={}, orderId={}, orderKey={}",
                    stepMessage.sagaId(), payload.orderId(), payload.orderKey());
        } catch (PaymentAlreadyRefundedException e) {
            log.info("이미 환불 처리된 결제 (멱등 처리). sagaId={}, message={}",
                    stepMessage.sagaId(), e.getMessage());
            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true,\"skipped\":true}");
        } catch (Exception e) {
            log.error("SAGA PG 환불 처리 실패. sagaId={}, error={}",
                    stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "PG 환불 처리 실패");
        }
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA PG 환불 스텝 페이로드 검증 실패. sagaId={}, reason={}",
                stepMessage.sagaId(), reason);
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason);
    }
}
