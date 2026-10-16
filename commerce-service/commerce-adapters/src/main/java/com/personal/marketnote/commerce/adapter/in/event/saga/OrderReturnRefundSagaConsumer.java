package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.ReturnRefundSagaPayload;
import com.personal.marketnote.commerce.exception.PaymentAlreadyRefundedException;
import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnRefundCommand;
import com.personal.marketnote.commerce.port.in.usecase.returntracker.CompleteReturnRefundUseCase;
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
public class OrderReturnRefundSagaConsumer {

    private final ObjectMapper objectMapper;
    private final CompleteReturnRefundUseCase completeReturnRefundUseCase;
    private final SagaResponsePublisher sagaResponsePublisher;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_RETURN_REFUND,
            groupId = "saga-order-return-refund"
    )
    public void handleRefundStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        try {
            EventEnvelope<?> envelope = record.value();

            if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
                return;
            }

            SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

            log.info("SAGA 반품 PG 환불 스텝 수신. sagaId={}, messageType={}", stepMessage.sagaId(), stepMessage.messageType());

            if (SagaStepMessage.ACTION.equals(stepMessage.messageType())) {
                handleAction(stepMessage);
                return;
            }

            log.warn("반품 PG 환불 스텝은 보상이 없음. sagaId={}, messageType={}",
                    stepMessage.sagaId(), stepMessage.messageType()
            );
        } catch (Exception e) {
            log.error("SAGA 반품 PG 환불 스텝 처리 실패. topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e
            );
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            ReturnRefundSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), ReturnRefundSagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }

            if (FormatValidator.hasNoValue(payload.orderKey())) {
                publishValidationFailure(stepMessage, "orderKey 누락");
                return;
            }

            CompleteReturnRefundCommand command = CompleteReturnRefundCommand.builder()
                    .orderId(payload.orderId())
                    .orderKey(payload.orderKey())
                    .buyerId(payload.buyerId())
                    .returnAmount(payload.returnAmount())
                    .paymentAmount(payload.paymentAmount())
                    .isFullReturn(payload.isFullReturn())
                    .returnShippingFee(payload.returnShippingFee() != null ? payload.returnShippingFee() : 0L)
                    .build();

            completeReturnRefundUseCase.completeReturnRefund(command);

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}"
            );

            log.info("SAGA 반품 PG 환불 성공. sagaId={}, orderId={}", stepMessage.sagaId(), payload.orderId());
        } catch (PaymentAlreadyRefundedException e) {
            log.info("이미 환불 처리된 결제 (멱등 처리). sagaId={}, message={}", stepMessage.sagaId(), e.getMessage());
            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true,\"idempotent\":true}"
            );
        } catch (Exception e) {
            log.error("SAGA 반품 PG 환불 처리 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "반품 PG 환불 처리 실패"
            );
        }
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 반품 PG 환불 스텝 페이로드 검증 실패. sagaId={}, reason={}", stepMessage.sagaId(), reason);
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason
        );
    }
}
