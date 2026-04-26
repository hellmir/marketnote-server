package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.exception.DuplicateLedgerTransactionException;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.SettlementExecutedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementExecutedLedgerConsumer {
    private final ObjectMapper objectMapper;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @KafkaListener(
            topics = KafkaTopicConstants.SETTLEMENT_EXECUTED,
            groupId = "commerce-ledger"
    )
    public void handleSettlementExecutedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.SETTLEMENT_EXECUTED)) {
            acknowledgment.acknowledge();
            return;
        }

        SettlementExecutedEvent payload = envelope.getPayloadAs(SettlementExecutedEvent.class, objectMapper);

        log.info("정산 실행 이벤트 수신 (회계 분개). eventId={}, settlementId={}, sellerId={}",
                envelope.eventId(), payload.settlementId(), payload.sellerId());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("settlementId", payload.settlementId()))) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            long shippingFee = payload.shippingFee() != null ? payload.shippingFee() : 0L;
            long feeBase = Math.addExact(payload.totalAllocatedAmount(), shippingFee);

            recordLedgerEntryUseCase.recordPgSettlement(
                    payload.settlementId(), feeBase, payload.pgFeeAmount()
            );

            long sellerSettlementDebit = Math.addExact(payload.sellerPayoutAmount(), payload.platformFeeAmount());
            recordLedgerEntryUseCase.recordSellerSettlement(
                    payload.settlementId(), sellerSettlementDebit,
                    payload.sellerPayoutAmount(), payload.platformFeeAmount()
            );

            log.info("정산 분개 완료. settlementId={}, totalAllocated={}, pgFee={}, sellerPayout={}, platformFee={}",
                    payload.settlementId(), payload.totalAllocatedAmount(), payload.pgFeeAmount(),
                    payload.sellerPayoutAmount(), payload.platformFeeAmount());
        } catch (DuplicateLedgerTransactionException e) {
            log.info("이미 처리된 정산 분개 이벤트 (멱등 처리). eventId={}, message={}",
                    envelope.eventId(), e.getMessage());
        }
        // 그 외 예외는 DefaultErrorHandler가 재시도 + DLT로 처리

        acknowledgment.acknowledge();
    }
}
