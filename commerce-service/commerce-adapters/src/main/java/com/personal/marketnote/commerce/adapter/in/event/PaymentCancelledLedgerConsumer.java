package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.DuplicateLedgerTransactionException;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.PaymentCancelledEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelledLedgerConsumer {
    private final ObjectMapper objectMapper;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @KafkaListener(
            topics = KafkaTopicConstants.PAYMENT_CANCELLED,
            groupId = "commerce-ledger"
    )
    public void handlePaymentCancelledEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.PAYMENT_CANCELLED)) {
            acknowledgment.acknowledge();
            return;
        }

        PaymentCancelledEvent payload = envelope.getPayloadAs(PaymentCancelledEvent.class, objectMapper);

        log.info("결제 취소 이벤트 수신 (회계 역분개). eventId={}, orderId={}, isFullCancel={}, cancelAmount={}",
                envelope.eventId(), payload.orderId(), payload.isFullCancel(), payload.cancelAmount());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()))) {
            acknowledgment.acknowledge();
            return;
        }

        String idempotencyKey = resolveIdempotencyKey(payload);

        try {
            recordLedgerEntryUseCase.recordPaymentCancellation(
                    payload.orderId(), payload.cancelAmount(), idempotencyKey
            );
            log.info("결제 취소 역분개 완료. orderId={}, isFullCancel={}, cancelAmount={}, idempotencyKey={}",
                    payload.orderId(), payload.isFullCancel(), payload.cancelAmount(), idempotencyKey);
        } catch (DuplicateLedgerTransactionException e) {
            log.info("이미 처리된 결제 취소 역분개 이벤트 (멱등 처리). eventId={}, message={}",
                    envelope.eventId(), e.getMessage());
        }
        // 그 외 예외는 DefaultErrorHandler가 재시도 + DLT로 처리

        acknowledgment.acknowledge();
    }

    private String resolveIdempotencyKey(PaymentCancelledEvent payload) {
        if (payload.isFullCancel()) {
            return "PAYMENT_CANCELLATION:" + payload.orderId();
        }
        return "PAYMENT_PARTIAL_REFUND:" + payload.orderId()
                + ":" + (FormatValidator.hasValue(payload.cancelId()) ? payload.cancelId() : "unknown");
    }
}
