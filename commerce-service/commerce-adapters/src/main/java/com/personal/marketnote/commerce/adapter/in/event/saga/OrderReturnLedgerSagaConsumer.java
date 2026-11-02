package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.ReturnLedgerCompensationSagaPayload;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.ReturnLedgerSagaPayload;
import com.personal.marketnote.commerce.exception.DuplicateLedgerTransactionException;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
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
public class OrderReturnLedgerSagaConsumer {

    private final ObjectMapper objectMapper;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;
    private final SagaResponsePublisher sagaResponsePublisher;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_RETURN_LEDGER,
            groupId = "saga-order-return-ledger"
    )
    public void handleLedgerStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        try {
            EventEnvelope<?> envelope = record.value();

            if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
                return;
            }

            SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

            log.info("SAGA 반품 역분개 스텝 수신. sagaId={}, messageType={}", stepMessage.sagaId(), stepMessage.messageType());

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
            log.error("SAGA 반품 역분개 스텝 처리 실패. topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e
            );
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            ReturnLedgerSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), ReturnLedgerSagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }

            recordLedgerEntryUseCase.recordPaymentCancellation(
                    payload.orderId(), payload.returnAmount(), payload.idempotencyKey()
            );

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}"
            );

            log.info("SAGA 반품 역분개 성공. sagaId={}, orderId={}", stepMessage.sagaId(), payload.orderId());
        } catch (DuplicateLedgerTransactionException e) {
            log.info("이미 처리된 반품 역분개 (멱등 처리). sagaId={}", stepMessage.sagaId());
            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true,\"idempotent\":true}"
            );
        } catch (Exception e) {
            log.error("SAGA 반품 역분개 처리 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "반품 역분개 처리 실패"
            );
        }
    }

    private void handleCompensation(SagaStepMessage stepMessage) {
        try {
            ReturnLedgerCompensationSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), ReturnLedgerCompensationSagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }

            recordLedgerEntryUseCase.recordPaymentApproval(payload.orderId(), payload.paymentAmount());

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true}"
            );

            log.info("SAGA 반품 역분개 보상 완료 (원래 분개 복구). sagaId={}, orderId={}",
                    stepMessage.sagaId(), payload.orderId()
            );
        } catch (DuplicateLedgerTransactionException e) {
            log.info("이미 처리된 역분개 보상 (멱등 처리). sagaId={}", stepMessage.sagaId());
            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true,\"idempotent\":true}"
            );
        } catch (Exception e) {
            log.error("SAGA 반품 역분개 보상 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "반품 역분개 보상 처리 실패"
            );
        }
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 반품 역분개 스텝 페이로드 검증 실패. sagaId={}, reason={}", stepMessage.sagaId(), reason);
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason
        );
    }
}
