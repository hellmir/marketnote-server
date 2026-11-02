package com.personal.marketnote.reward.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.adapter.in.event.saga.payload.OrderPointSagaPayload;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
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
public class OrderPaymentCompletedOrderPointSagaConsumer {

    private static final String DEDUCTION_REASON = "주문 포인트 사용";
    private static final String COMPENSATION_REASON = "SAGA 보상 - 주문 포인트 환원";

    private final ObjectMapper objectMapper;
    private final ModifyUserPointUseCase modifyUserPointUseCase;
    private final SagaResponsePublisher sagaResponsePublisher;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_ORDER_POINT,
            groupId = "saga-order-payment-downstream-order-point"
    )
    public void handleOrderPointStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        try {
            EventEnvelope<?> envelope = record.value();

            if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
                return;
            }

            SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

            log.info("SAGA 결제 완료 주문 포인트 스텝 수신. sagaId={}, messageType={}",
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
            log.error("SAGA 결제 완료 주문 포인트 스텝 처리 실패. topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e
            );
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            OrderPointSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), OrderPointSagaPayload.class
            );

            if (FormatValidator.hasNoValue(payload.orderId())
                    || FormatValidator.hasNoValue(payload.buyerId())) {
                publishValidationFailure(stepMessage, "orderId 또는 buyerId 누락");
                return;
            }

            if (FormatValidator.hasNoValue(payload.pointAmount()) || payload.pointAmount() <= 0) {
                log.info("포인트 미사용 주문 (차감 생략). sagaId={}, orderId={}",
                        stepMessage.sagaId(), payload.orderId()
                );
                sagaResponsePublisher.publishSuccess(
                        stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                        stepMessage.messageType(), "{\"success\":true,\"skipped\":true}"
                );
                return;
            }

            deductOrderPointIdempotent(payload);

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}"
            );

            log.info("SAGA 주문 포인트 차감 성공. sagaId={}, orderId={}, pointAmount={}",
                    stepMessage.sagaId(), payload.orderId(), payload.pointAmount()
            );
        } catch (Exception e) {
            log.error("SAGA 주문 포인트 차감 처리 실패. sagaId={}, error={}",
                    stepMessage.sagaId(), e.getMessage(), e
            );
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "주문 포인트 차감 처리 실패"
            );
        }
    }

    private void handleCompensation(SagaStepMessage stepMessage) {
        try {
            OrderPointSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), OrderPointSagaPayload.class
            );

            if (FormatValidator.hasNoValue(payload.orderId())
                    || FormatValidator.hasNoValue(payload.buyerId())) {
                publishValidationFailure(stepMessage, "orderId 또는 buyerId 누락");
                return;
            }

            if (FormatValidator.hasNoValue(payload.pointAmount()) || payload.pointAmount() <= 0) {
                log.info("보상할 주문 포인트 없음 (skip). sagaId={}", stepMessage.sagaId());
                sagaResponsePublisher.publishSuccess(
                        stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                        stepMessage.messageType(), "{\"compensated\":true,\"skipped\":true}"
                );
                return;
            }

            restoreOrderPointIdempotent(payload);

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true}"
            );

            log.info("SAGA 주문 포인트 환원 보상 완료. sagaId={}, orderId={}, pointAmount={}",
                    stepMessage.sagaId(), payload.orderId(), payload.pointAmount()
            );
        } catch (Exception e) {
            log.error("SAGA 주문 포인트 환원 보상 실패. sagaId={}, error={}",
                    stepMessage.sagaId(), e.getMessage(), e
            );
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "주문 포인트 환원 보상 처리 실패"
            );
        }
    }

    private void deductOrderPointIdempotent(OrderPointSagaPayload payload) {
        try {
            ModifyUserPointCommand command = ModifyUserPointCommand.builder()
                    .userId(payload.buyerId())
                    .changeType(UserPointChangeType.DEDUCTION)
                    .amount(payload.pointAmount())
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(payload.orderId())
                    .reason(DEDUCTION_REASON)
                    .build();
            modifyUserPointUseCase.modify(command);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 주문 포인트 차감 (멱등 처리). orderId={}, message={}",
                    payload.orderId(), e.getMessage()
            );
        }
    }

    private void restoreOrderPointIdempotent(OrderPointSagaPayload payload) {
        try {
            ModifyUserPointCommand command = ModifyUserPointCommand.builder()
                    .userId(payload.buyerId())
                    .changeType(UserPointChangeType.ACCRUAL)
                    .amount(payload.pointAmount())
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(payload.orderId())
                    .reason(COMPENSATION_REASON)
                    .build();
            modifyUserPointUseCase.modify(command);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 주문 포인트 환원 (멱등 처리). orderId={}, message={}",
                    payload.orderId(), e.getMessage()
            );
        }
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 주문 포인트 스텝 페이로드 검증 실패. sagaId={}, reason={}", stepMessage.sagaId(), reason);
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason
        );
    }
}
