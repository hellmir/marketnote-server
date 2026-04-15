package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.InventorySagaPayload;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.exception.DuplicateInventoryDeductionException;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReduceProductInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReleaseInventoryReservationUseCase;
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
public class OrderPaymentInventorySagaConsumer {

    private final ObjectMapper objectMapper;
    private final ReduceProductInventoryUseCase reduceProductInventoryUseCase;
    private final ReleaseInventoryReservationUseCase releaseInventoryReservationUseCase;
    private final SagaResponsePublisher sagaResponsePublisher;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_PAYMENT_INVENTORY,
            groupId = "saga-order-payment-inventory"
    )
    public void handleInventoryStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

        log.info("SAGA 재고 스텝 수신. sagaId={}, messageType={}",
                stepMessage.sagaId(), stepMessage.messageType());

        if (SagaStepMessage.ACTION.equals(stepMessage.messageType())) {
            handleAction(stepMessage);
            acknowledgment.acknowledge();
            return;
        }

        if (SagaStepMessage.COMPENSATION.equals(stepMessage.messageType())) {
            handleCompensation(stepMessage);
            acknowledgment.acknowledge();
            return;
        }

        log.warn("알 수 없는 messageType. sagaId={}, messageType={}",
                stepMessage.sagaId(), stepMessage.messageType());
        acknowledgment.acknowledge();
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            InventorySagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), InventorySagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }
            if (FormatValidator.hasNoValue(payload.orderProducts()) || payload.orderProducts().isEmpty()) {
                publishValidationFailure(stepMessage, "orderProducts 누락");
                return;
            }

            List<OrderProduct> orderProducts = SagaOrderProductMapper.toOrderProducts(payload.orderProducts());

            reduceProductInventoryUseCase.reduce(orderProducts, payload.orderId(), "SAGA 결제 완료 재고 차감");

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}");

            log.info("SAGA 재고 차감 성공. sagaId={}, orderId={}", stepMessage.sagaId(), payload.orderId());
        } catch (DuplicateInventoryDeductionException e) {
            log.info("이미 처리된 SAGA 재고 차감 (멱등 처리). sagaId={}", stepMessage.sagaId());
            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true,\"idempotent\":true}");
        } catch (Exception e) {
            log.error("SAGA 재고 차감 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "재고 차감 처리 실패");
        }
    }

    private void handleCompensation(SagaStepMessage stepMessage) {
        try {
            InventorySagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), InventorySagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }
            if (FormatValidator.hasNoValue(payload.orderProducts()) || payload.orderProducts().isEmpty()) {
                publishValidationFailure(stepMessage, "orderProducts 누락");
                return;
            }

            List<OrderProduct> orderProducts = SagaOrderProductMapper.toOrderProducts(payload.orderProducts());

            releaseInventoryReservationUseCase.release(orderProducts, payload.orderId(), "SAGA 보상 - 재고 복구");

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true}");

            log.info("SAGA 재고 복구 완료. sagaId={}, orderId={}", stepMessage.sagaId(), payload.orderId());
        } catch (Exception e) {
            log.error("SAGA 재고 복구 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "재고 복구 처리 실패");
        }
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 재고 스텝 페이로드 검증 실패. sagaId={}, reason={}", stepMessage.sagaId(), reason);
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason);
    }
}
