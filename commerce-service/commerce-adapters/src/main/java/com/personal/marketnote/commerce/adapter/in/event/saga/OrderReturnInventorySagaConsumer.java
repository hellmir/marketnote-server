package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.ReturnInventorySagaPayload;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.exception.DuplicateInventoryRestorationException;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReduceProductInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RestoreProductInventoryUseCase;
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

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderReturnInventorySagaConsumer {

    private final ObjectMapper objectMapper;
    private final RestoreProductInventoryUseCase restoreProductInventoryUseCase;
    private final ReduceProductInventoryUseCase reduceProductInventoryUseCase;
    private final SagaResponsePublisher sagaResponsePublisher;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_RETURN_INVENTORY,
            groupId = "saga-order-return-inventory"
    )
    public void handleInventoryStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        try {
            EventEnvelope<?> envelope = record.value();

            if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
                return;
            }

            SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

            log.info("SAGA 반품 재고 복구 스텝 수신. sagaId={}, messageType={}", stepMessage.sagaId(), stepMessage.messageType());

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
            log.error("SAGA 반품 재고 복구 스텝 처리 실패. topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e
            );
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            ReturnInventorySagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), ReturnInventorySagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }
            if (FormatValidator.hasNoValue(payload.returnProducts()) || payload.returnProducts().isEmpty()) {
                publishValidationFailure(stepMessage, "returnProducts 누락");
                return;
            }

            List<OrderProduct> returnProducts = SagaOrderProductMapper.toOrderProducts(payload.returnProducts());
            String reason = resolveReason(payload.isFullReturn());

            restoreProductInventoryUseCase.restore(returnProducts, payload.orderId(), reason);

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}"
            );

            log.info("SAGA 반품 재고 복구 성공. sagaId={}, orderId={}", stepMessage.sagaId(), payload.orderId());
        } catch (DuplicateInventoryRestorationException e) {
            log.info("이미 처리된 반품 재고 복구 (멱등 처리). sagaId={}", stepMessage.sagaId());
            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true,\"idempotent\":true}"
            );
        } catch (Exception e) {
            log.error("SAGA 반품 재고 복구 처리 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "반품 재고 복구 처리 실패"
            );
        }
    }

    private void handleCompensation(SagaStepMessage stepMessage) {
        try {
            ReturnInventorySagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), ReturnInventorySagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }
            if (FormatValidator.hasNoValue(payload.returnProducts()) || payload.returnProducts().isEmpty()) {
                publishValidationFailure(stepMessage, "returnProducts 누락");
                return;
            }

            List<OrderProduct> returnProducts = SagaOrderProductMapper.toOrderProducts(payload.returnProducts());

            reduceProductInventoryUseCase.reduce(
                    returnProducts, payload.orderId(), "SAGA 보상 - 반품 재고 복구 롤백"
            );

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true}"
            );

            log.info("SAGA 반품 재고 복구 보상 완료 (재차감). sagaId={}, orderId={}",
                    stepMessage.sagaId(), payload.orderId()
            );
        } catch (Exception e) {
            log.error("SAGA 반품 재고 복구 보상 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "반품 재고 복구 보상 처리 실패"
            );
        }
    }

    private String resolveReason(Boolean isFullReturn) {
        if (Boolean.TRUE.equals(isFullReturn)) {
            return "SAGA 반품 완료 전체 재고 복구";
        }
        return "SAGA 반품 완료 부분 재고 복구";
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 반품 재고 복구 스텝 페이로드 검증 실패. sagaId={}, reason={}", stepMessage.sagaId(), reason);
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason
        );
    }
}
