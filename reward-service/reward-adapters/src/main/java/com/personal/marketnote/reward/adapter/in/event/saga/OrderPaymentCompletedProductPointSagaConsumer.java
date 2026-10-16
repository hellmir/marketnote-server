package com.personal.marketnote.reward.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.adapter.in.event.saga.payload.ProductPointSagaPayload;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.CancelPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.CancelPendingPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingPointUseCase;
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
public class OrderPaymentCompletedProductPointSagaConsumer {

    private static final String PRODUCT_PURCHASE_REASON = "상품 구매 적립";
    private static final String PRODUCT_COMPENSATION_REASON = "결제 취소 적립 예정 포인트 회수";

    private final ObjectMapper objectMapper;
    private final ModifyPendingPointUseCase modifyPendingPointUseCase;
    private final CancelPendingPointUseCase cancelPendingPointUseCase;
    private final SagaResponsePublisher sagaResponsePublisher;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_PRODUCT_POINT,
            groupId = "saga-order-payment-downstream-product-point"
    )
    public void handleProductPointStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        try {
            EventEnvelope<?> envelope = record.value();

            if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
                return;
            }

            SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

            log.info("SAGA 결제 완료 상품 적립 예정 스텝 수신. sagaId={}, messageType={}",
                    stepMessage.sagaId(), stepMessage.messageType()
            );

            if (SagaStepMessage.ACTION.equals(stepMessage.messageType())) {
                handleAction(stepMessage);
                return;
            }

            if (SagaStepMessage.COMPENSATION.equals(stepMessage.messageType())) {
                handleCompensation(stepMessage);
                return;
            }

            log.warn("알 수 없는 messageType. sagaId={}, messageType={}",
                    stepMessage.sagaId(), stepMessage.messageType()
            );
        } catch (Exception e) {
            log.error("SAGA 결제 완료 상품 적립 예정 스텝 처리 실패. topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e
            );
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            ProductPointSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), ProductPointSagaPayload.class
            );

            if (FormatValidator.hasNoValue(payload.orderId())
                    || FormatValidator.hasNoValue(payload.buyerId())) {
                publishValidationFailure(stepMessage, "orderId 또는 buyerId 누락");
                return;
            }

            if (FormatValidator.hasNoValue(payload.totalAmount()) || payload.totalAmount() <= 0) {
                skipSuccess(stepMessage, "결제 금액 없음");
                return;
            }

            if (FormatValidator.hasNoValue(payload.totalAccumulatedPoint())
                    || payload.totalAccumulatedPoint() <= 0) {
                skipSuccess(stepMessage, "상품 적립 포인트 없음");
                return;
            }

            accrueProductPointIdempotent(payload);

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}"
            );

            log.info("SAGA 상품 적립 예정 포인트 성공. sagaId={}, orderId={}, totalAccumulatedPoint={}",
                    stepMessage.sagaId(), payload.orderId(), payload.totalAccumulatedPoint()
            );
        } catch (Exception e) {
            log.error("SAGA 상품 적립 예정 포인트 처리 실패. sagaId={}, error={}",
                    stepMessage.sagaId(), e.getMessage(), e
            );
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "상품 적립 예정 포인트 처리 실패"
            );
        }
    }

    private void handleCompensation(SagaStepMessage stepMessage) {
        try {
            ProductPointSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), ProductPointSagaPayload.class
            );

            if (FormatValidator.hasNoValue(payload.orderId())
                    || FormatValidator.hasNoValue(payload.buyerId())) {
                publishValidationFailure(stepMessage, "orderId 또는 buyerId 누락");
                return;
            }

            CancelPendingPointCommand command = CancelPendingPointCommand.builder()
                    .userId(payload.buyerId())
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(payload.orderId())
                    .reason(PRODUCT_COMPENSATION_REASON)
                    .build();
            cancelPendingPointUseCase.cancelPending(command);

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true}"
            );

            log.info("SAGA 상품 적립 예정 포인트 보상 완료. sagaId={}, orderId={}, buyerId={}",
                    stepMessage.sagaId(), payload.orderId(), payload.buyerId()
            );
        } catch (Exception e) {
            log.error("SAGA 상품 적립 예정 포인트 보상 실패. sagaId={}, error={}",
                    stepMessage.sagaId(), e.getMessage(), e
            );
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "상품 적립 예정 포인트 보상 처리 실패"
            );
        }
    }

    private void accrueProductPointIdempotent(ProductPointSagaPayload payload) {
        try {
            ModifyPendingPointCommand command = ModifyPendingPointCommand.builder()
                    .userId(payload.buyerId())
                    .changeType(UserPointChangeType.ACCRUAL)
                    .amount(payload.totalAccumulatedPoint())
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(payload.orderId())
                    .reason(PRODUCT_PURCHASE_REASON)
                    .build();
            modifyPendingPointUseCase.modifyPending(command);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 상품 적립 예정 포인트 (멱등 처리). orderId={}, message={}",
                    payload.orderId(), e.getMessage()
            );
        }
    }

    private void skipSuccess(SagaStepMessage stepMessage, String reason) {
        log.info("상품 적립 예정 포인트 skip (reason={}). sagaId={}", reason, stepMessage.sagaId());
        sagaResponsePublisher.publishSuccess(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "{\"success\":true,\"skipped\":true}"
        );
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 상품 적립 예정 포인트 스텝 페이로드 검증 실패. sagaId={}, reason={}",
                stepMessage.sagaId(), reason
        );
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason
        );
    }
}
