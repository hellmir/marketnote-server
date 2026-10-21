package com.personal.marketnote.reward.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.adapter.in.event.saga.payload.SharedPointSagaPayload;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.exception.UserPointNotFoundException;
import com.personal.marketnote.reward.port.in.command.point.CancelPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingSharedPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.CancelPendingPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingSharedPointUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderPaymentCompletedSharedPointSagaConsumer {

    private static final String SHARE_PURCHASE_REASON = "링크 공유 회원 상품 구매";
    private static final String SHARE_COMPENSATION_REASON = "결제 취소 적립 예정 포인트 회수";

    private final ObjectMapper objectMapper;
    private final ModifyPendingSharedPointUseCase modifyPendingSharedPointUseCase;
    private final CancelPendingPointUseCase cancelPendingPointUseCase;
    private final FindUserPointPort findUserPointPort;
    private final SagaResponsePublisher sagaResponsePublisher;

    @Value("${reward.share-point-rate:0.1}")
    private BigDecimal sharePointRate;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_SHARED_POINT,
            groupId = "saga-order-payment-downstream-shared-point"
    )
    public void handleSharedPointStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        try {
            EventEnvelope<?> envelope = record.value();

            if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
                return;
            }

            SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

            log.info("SAGA 결제 완료 공유 적립 예정 스텝 수신. sagaId={}, messageType={}",
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
            log.error("SAGA 결제 완료 공유 적립 예정 스텝 처리 실패. topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e
            );
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            SharedPointSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), SharedPointSagaPayload.class
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

            List<UUID> sharerKeys = payload.sharerKeys();
            if (FormatValidator.hasNoValue(sharerKeys) || sharerKeys.isEmpty()) {
                skipSuccess(stepMessage, "공유자 없음");
                return;
            }

            long sharePointAmount = calculateSharedPoint(payload.totalAmount(), sharePointRate);
            if (sharePointAmount <= 0) {
                skipSuccess(stepMessage, "공유 포인트 금액 0 이하");
                return;
            }

            for (UUID sharerKey : sharerKeys) {
                accrueSharedPointIdempotent(payload, sharerKey, sharePointAmount);
            }

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}"
            );

            log.info("SAGA 공유 적립 예정 포인트 성공. sagaId={}, orderId={}, sharerKeys={}",
                    stepMessage.sagaId(), payload.orderId(), sharerKeys
            );
        } catch (Exception e) {
            log.error("SAGA 공유 적립 예정 포인트 처리 실패. sagaId={}, error={}",
                    stepMessage.sagaId(), e.getMessage(), e
            );
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "공유 적립 예정 포인트 처리 실패"
            );
        }
    }

    private void handleCompensation(SagaStepMessage stepMessage) {
        try {
            SharedPointSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), SharedPointSagaPayload.class
            );

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }

            List<UUID> sharerKeys = payload.sharerKeys();
            if (FormatValidator.hasNoValue(sharerKeys) || sharerKeys.isEmpty()) {
                log.info("공유자 없음 (보상 skip). sagaId={}", stepMessage.sagaId());
                sagaResponsePublisher.publishSuccess(
                        stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                        stepMessage.messageType(), "{\"compensated\":true,\"skipped\":true}"
                );
                return;
            }

            for (UUID sharerKey : sharerKeys) {
                cancelSharedPendingPoint(payload.orderId(), sharerKey);
            }

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true}"
            );

            log.info("SAGA 공유 적립 예정 포인트 보상 완료. sagaId={}, orderId={}, sharerKeys={}",
                    stepMessage.sagaId(), payload.orderId(), sharerKeys
            );
        } catch (Exception e) {
            log.error("SAGA 공유 적립 예정 포인트 보상 실패. sagaId={}, error={}",
                    stepMessage.sagaId(), e.getMessage(), e
            );
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "공유 적립 예정 포인트 보상 처리 실패"
            );
        }
    }

    private void accrueSharedPointIdempotent(SharedPointSagaPayload payload, UUID sharerKey, long amount) {
        try {
            ModifyPendingSharedPointCommand command = ModifyPendingSharedPointCommand.builder()
                    .buyerId(payload.buyerId())
                    .sharerKey(sharerKey)
                    .changeType(UserPointChangeType.ACCRUAL)
                    .amount(amount)
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(payload.orderId())
                    .reason(SHARE_PURCHASE_REASON)
                    .build();
            modifyPendingSharedPointUseCase.modifyPending(command);
        } catch (DuplicateUserPointHistoryException e) {
            log.info("이미 처리된 공유 적립 예정 포인트 (멱등 처리). sharerKey={}, message={}",
                    sharerKey, e.getMessage()
            );
        }
    }

    private void cancelSharedPendingPoint(Long orderId, UUID sharerKey) {
        UserPoint userPoint = findUserPointPort.findByUserKey(sharerKey.toString())
                .orElseThrow(() -> new UserPointNotFoundException(sharerKey.toString()));

        CancelPendingPointCommand command = CancelPendingPointCommand.builder()
                .userId(userPoint.getUserId())
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(orderId)
                .reason(SHARE_COMPENSATION_REASON)
                .build();
        cancelPendingPointUseCase.cancelPending(command);
    }

    private void skipSuccess(SagaStepMessage stepMessage, String reason) {
        log.info("공유 적립 예정 포인트 skip (reason={}). sagaId={}", reason, stepMessage.sagaId());
        sagaResponsePublisher.publishSuccess(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "{\"success\":true,\"skipped\":true}"
        );
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 공유 적립 예정 포인트 스텝 페이로드 검증 실패. sagaId={}, reason={}",
                stepMessage.sagaId(), reason
        );
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason
        );
    }

    private static long calculateSharedPoint(long amount, BigDecimal rate) {
        return BigDecimal.valueOf(amount)
                .multiply(rate)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
