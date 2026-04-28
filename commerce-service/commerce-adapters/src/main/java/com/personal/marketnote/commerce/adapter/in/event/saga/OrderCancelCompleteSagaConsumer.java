package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.CompleteCancellationSagaPayload;
import com.personal.marketnote.commerce.port.in.command.order.CompleteCancelOrderCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.CompleteCancelOrderUseCase;
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
public class OrderCancelCompleteSagaConsumer {

    private final ObjectMapper objectMapper;
    private final CompleteCancelOrderUseCase completeCancelOrderUseCase;
    private final SagaResponsePublisher sagaResponsePublisher;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_CANCEL_COMPLETED,
            groupId = "saga-order-cancel-completed"
    )
    public void handleCompleteCancellationStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        try {
            EventEnvelope<?> envelope = record.value();

            if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
                return;
            }

            SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

            log.info("SAGA 주문 취소 완료 스텝 수신. sagaId={}, messageType={}",
                    stepMessage.sagaId(), stepMessage.messageType());

            if (SagaStepMessage.ACTION.equals(stepMessage.messageType())) {
                handleAction(stepMessage);
                return;
            }

            log.warn("주문 취소 완료 스텝은 보상이 없음. 알 수 없는 messageType. sagaId={}, messageType={}",
                    stepMessage.sagaId(), stepMessage.messageType());
        } catch (Exception e) {
            log.error("SAGA 주문 취소 완료 스텝 메시지 처리 실패. topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e);
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            CompleteCancellationSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), CompleteCancellationSagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }

            if (FormatValidator.hasNoValue(payload.orderKey())) {
                publishValidationFailure(stepMessage, "orderKey 누락");
                return;
            }

            CompleteCancelOrderCommand command = CompleteCancelOrderCommand.builder()
                    .orderId(payload.orderId())
                    .orderKey(payload.orderKey())
                    .buyerId(payload.buyerId())
                    .cancelAmount(payload.cancelAmount())
                    .paymentAmount(payload.paymentAmount())
                    .pointAmount(payload.pointAmount())
                    .shippingFee(payload.shippingFee())
                    .isFullCancel(payload.isFullCancel())
                    .alreadyRefunded(payload.alreadyRefunded())
                    .reasonCategory(payload.reasonCategory())
                    .reason(payload.reason())
                    .build();

            completeCancelOrderUseCase.completeCancellation(command);

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}");

            log.info("SAGA 주문 취소 완료 처리 성공. sagaId={}, orderId={}, orderKey={}",
                    stepMessage.sagaId(), payload.orderId(), payload.orderKey());
        } catch (Exception e) {
            log.error("SAGA 주문 취소 완료 처리 실패. sagaId={}, error={}",
                    stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "주문 취소 완료 처리 실패");
        }
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 주문 취소 완료 스텝 페이로드 검증 실패. sagaId={}, reason={}",
                stepMessage.sagaId(), reason);
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason);
    }
}
