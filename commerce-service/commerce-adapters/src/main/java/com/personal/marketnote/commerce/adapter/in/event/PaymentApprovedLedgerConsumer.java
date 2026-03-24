package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.DuplicateLedgerTransactionException;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.PaymentApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentApprovedLedgerConsumer {
    private final ObjectMapper objectMapper;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @KafkaListener(
            topics = KafkaTopicConstants.PAYMENT_APPROVED,
            groupId = "commerce-ledger"
    )
    public void handlePaymentApprovedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.PAYMENT_APPROVED)) {
            acknowledgment.acknowledge();
            return;
        }

        PaymentApprovedEvent payload = envelope.getPayloadAs(PaymentApprovedEvent.class, objectMapper);

        log.info("결제 승인 이벤트 수신 (회계 분개). eventId={}, orderId={}, orderKey={}, paymentAmount={}",
                envelope.eventId(), payload.orderId(), payload.orderKey(), payload.paymentAmount());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()))) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            recordLedgerEntryUseCase.recordPaymentApproval(payload.orderId(), payload.paymentAmount());
            log.info("결제 승인 분개 완료. orderId={}, paymentAmount={}", payload.orderId(), payload.paymentAmount());
        } catch (DuplicateLedgerTransactionException e) {
            log.info("이미 처리된 결제 승인 분개 이벤트 (멱등 처리). eventId={}, message={}",
                    envelope.eventId(), e.getMessage());
        }
        // 그 외 예외는 DefaultErrorHandler가 재시도 + DLT로 처리

        acknowledgment.acknowledge();
    }
}
