package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.ReturnSettlementSagaPayload;
import com.personal.marketnote.commerce.domain.ledger.IdempotencyKey;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationCreateState;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTargetType;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTransactionType;
import com.personal.marketnote.commerce.port.out.settlement.DeletePaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.SavePaymentAllocationPort;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderReturnSettlementSagaConsumer {

    private static final String IDEMPOTENCY_KEY_PREFIX = "SAGA_ORDER_RETURN_ALLOCATION:";

    private final ObjectMapper objectMapper;
    private final FindPaymentAllocationPort findPaymentAllocationPort;
    private final SavePaymentAllocationPort savePaymentAllocationPort;
    private final DeletePaymentAllocationPort deletePaymentAllocationPort;
    private final SagaResponsePublisher sagaResponsePublisher;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_RETURN_SETTLEMENT,
            groupId = "saga-order-return-settlement"
    )
    public void handleSettlementStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        try {
            EventEnvelope<?> envelope = record.value();

            if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
                return;
            }

            SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

            log.info("SAGA 반품 정산 취소 스텝 수신. sagaId={}, messageType={}", stepMessage.sagaId(), stepMessage.messageType());

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
            log.error("SAGA 반품 정산 취소 스텝 처리 실패. topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e
            );
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            ReturnSettlementSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), ReturnSettlementSagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }

            createReturnAllocations(payload.orderId());

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}"
            );

            log.info("SAGA 반품 정산 역배분 생성 완료. sagaId={}, orderId={}", stepMessage.sagaId(), payload.orderId());
        } catch (DataIntegrityViolationException e) {
            log.info("이미 처리된 반품 정산 역배분 (멱등 처리). sagaId={}", stepMessage.sagaId());
            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true,\"idempotent\":true}"
            );
        } catch (Exception e) {
            log.error("SAGA 반품 정산 역배분 처리 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "반품 정산 역배분 처리 실패"
            );
        }
    }

    private void handleCompensation(SagaStepMessage stepMessage) {
        try {
            ReturnSettlementSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), ReturnSettlementSagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }

            deletePaymentAllocationPort.deleteReturnAllocationsByOrderId(payload.orderId());

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true}"
            );

            log.info("SAGA 반품 정산 역배분 보상 완료 (삭제). sagaId={}, orderId={}",
                    stepMessage.sagaId(), payload.orderId()
            );
        } catch (Exception e) {
            log.error("SAGA 반품 정산 역배분 보상 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "반품 정산 역배분 보상 처리 실패"
            );
        }
    }

    private void createReturnAllocations(Long orderId) {
        List<PaymentAllocation> originalAllocations = findPaymentAllocationPort.findByOrderId(orderId);

        List<PaymentAllocation> returnAllocations = originalAllocations.stream()
                .filter(allocation -> allocation.getTransactionType().isPositiveContribution())
                .map(allocation -> PaymentAllocation.from(
                        PaymentAllocationCreateState.builder()
                                .orderId(orderId)
                                .sellerId(allocation.getSellerId())
                                .allocatedAmount(allocation.getAllocatedAmount().getValue())
                                .shippingFee(allocation.getShippingFee().getValue())
                                .transactionType(PaymentAllocationTransactionType.RETURN_REFUND)
                                .targetType(PaymentAllocationTargetType.ORDER)
                                .idempotencyKey(IdempotencyKey.of(
                                        IDEMPOTENCY_KEY_PREFIX + orderId + ":" + allocation.getSellerId()
                                ))
                                .build()
                ))
                .toList();

        if (!returnAllocations.isEmpty()) {
            savePaymentAllocationPort.saveAll(returnAllocations);
        }
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 반품 정산 취소 스텝 페이로드 검증 실패. sagaId={}, reason={}", stepMessage.sagaId(), reason);
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason
        );
    }
}
