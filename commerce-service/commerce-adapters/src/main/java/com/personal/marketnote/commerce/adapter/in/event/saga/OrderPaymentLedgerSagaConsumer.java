package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.LedgerActionSagaPayload;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.LedgerCompensationSagaPayload;
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
public class OrderPaymentLedgerSagaConsumer {

    private final ObjectMapper objectMapper;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;
    private final SagaResponsePublisher sagaResponsePublisher;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_PAYMENT_LEDGER,
            groupId = "saga-order-payment-ledger"
    )
    public void handleLedgerStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

        log.info("SAGA 분개 스텝 수신. sagaId={}, messageType={}",
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
            LedgerActionSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), LedgerActionSagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId()) || FormatValidator.hasNoValue(payload.paymentAmount())) {
                publishValidationFailure(stepMessage, "orderId 또는 paymentAmount 누락");
                return;
            }

            recordLedgerEntryUseCase.recordPaymentApproval(payload.orderId(), payload.paymentAmount());

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}");

            log.info("SAGA 결제 승인 분개 성공. sagaId={}, orderId={}, paymentAmount={}",
                    stepMessage.sagaId(), payload.orderId(), payload.paymentAmount());
        } catch (DuplicateLedgerTransactionException e) {
            log.info("이미 처리된 SAGA 분개 (멱등 처리). sagaId={}", stepMessage.sagaId());
            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true,\"idempotent\":true}");
        } catch (Exception e) {
            log.error("SAGA 분개 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "분개 처리 실패");
        }
    }

    private void handleCompensation(SagaStepMessage stepMessage) {
        try {
            LedgerCompensationSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), LedgerCompensationSagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId()) || FormatValidator.hasNoValue(payload.cancelAmount())
                    || FormatValidator.hasNoValue(payload.idempotencyKey())) {
                publishValidationFailure(stepMessage, "orderId, cancelAmount 또는 idempotencyKey 누락");
                return;
            }

            recordLedgerEntryUseCase.recordPaymentCancellation(
                    payload.orderId(), payload.cancelAmount(), payload.idempotencyKey());

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true}");

            log.info("SAGA 역분개 완료. sagaId={}, orderId={}", stepMessage.sagaId(), payload.orderId());
        } catch (DuplicateLedgerTransactionException e) {
            log.info("이미 처리된 SAGA 역분개 (멱등 처리). sagaId={}", stepMessage.sagaId());
            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true,\"idempotent\":true}");
        } catch (Exception e) {
            log.error("SAGA 역분개 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "역분개 처리 실패");
        }
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 분개 스텝 페이로드 검증 실패. sagaId={}, reason={}", stepMessage.sagaId(), reason);
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason);
    }
}
