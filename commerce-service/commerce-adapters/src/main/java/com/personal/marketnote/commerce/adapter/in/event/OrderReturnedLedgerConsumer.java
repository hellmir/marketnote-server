package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.DuplicateLedgerTransactionException;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderReturnedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReturnedLedgerConsumer {
    private static final String IDEMPOTENCY_KEY_PREFIX = "ORDER_RETURN:";

    private final ObjectMapper objectMapper;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_RETURNED,
            groupId = "commerce-order-returned-ledger"
    )
    public void handleOrderReturnedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.ORDER_RETURNED)) {
            acknowledgment.acknowledge();
            return;
        }

        OrderReturnedEvent payload = envelope.getPayloadAs(OrderReturnedEvent.class, objectMapper);

        log.info("반품 완료 이벤트 수신 (회계 역분개). eventId={}, orderId={}, isFullReturn={}, returnAmount={}",
                envelope.eventId(), payload.orderId(), payload.isFullReturn(), payload.returnAmount());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()))) {
            acknowledgment.acknowledge();
            return;
        }

        String idempotencyKey = IDEMPOTENCY_KEY_PREFIX + payload.orderId();

        try {
            recordLedgerEntryUseCase.recordPaymentCancellation(
                    payload.orderId(), payload.returnAmount(), idempotencyKey
            );
            log.info("반품 완료 역분개 완료. orderId={}, returnAmount={}, idempotencyKey={}",
                    payload.orderId(), payload.returnAmount(), idempotencyKey);
        } catch (DuplicateLedgerTransactionException e) {
            log.info("이미 처리된 반품 완료 역분개 이벤트 (멱등 처리). eventId={}, message={}",
                    envelope.eventId(), e.getMessage());
        }

        acknowledgment.acknowledge();
    }
}
