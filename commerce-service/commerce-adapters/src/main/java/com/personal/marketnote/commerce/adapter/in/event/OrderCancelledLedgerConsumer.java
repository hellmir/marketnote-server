package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.DuplicateLedgerTransactionException;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelledLedgerConsumer {
    private static final String IDEMPOTENCY_KEY_PREFIX = "ORDER_CANCELLATION:";

    private final ObjectMapper objectMapper;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_CANCELLED,
            groupId = "commerce-order-cancelled-ledger"
    )
    public void handleOrderCancelledEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.ORDER_CANCELLED)) {
            acknowledgment.acknowledge();
            return;
        }

        OrderCancelledEvent payload = envelope.getPayloadAs(OrderCancelledEvent.class, objectMapper);

        log.info("주문 취소 이벤트 수신 (회계 역분개). eventId={}, orderId={}, isFullCancel={}, cancelAmount={}",
                envelope.eventId(), payload.orderId(), payload.isFullCancel(), payload.cancelAmount());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("orderId", payload.orderId()))) {
            acknowledgment.acknowledge();
            return;
        }

        String idempotencyKey = IDEMPOTENCY_KEY_PREFIX + payload.orderId();

        try {
            recordLedgerEntryUseCase.recordPaymentCancellation(
                    payload.orderId(), payload.cancelAmount(), idempotencyKey
            );
            log.info("주문 취소 역분개 완료. orderId={}, cancelAmount={}, idempotencyKey={}",
                    payload.orderId(), payload.cancelAmount(), idempotencyKey);
        } catch (DuplicateLedgerTransactionException e) {
            log.info("이미 처리된 주문 취소 역분개 이벤트 (멱등 처리). eventId={}, message={}",
                    envelope.eventId(), e.getMessage());
        }

        acknowledgment.acknowledge();
    }
}
