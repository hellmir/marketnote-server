package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.SettlementExecutedEvent;
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
public class SettlementExecutedLedgerConsumer {
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.SETTLEMENT_EXECUTED,
            groupId = "commerce-ledger"
    )
    public void handleSettlementExecutedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        try {
            SettlementExecutedEvent payload = envelope.getPayloadAs(SettlementExecutedEvent.class, objectMapper);

            log.info("정산 실행 이벤트 수신 (회계 분개). eventId={}, settlementId={}, sellerId={}",
                    envelope.eventId(), payload.settlementId(), payload.sellerId());

            if (FormatValidator.hasNoValue(payload.settlementId())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, settlementId=null",
                        envelope.eventId());
                acknowledgment.acknowledge();
                return;
            }

            // FIXME: [#929][#1024] HTTP 제거 후 RecordLedgerEntryUseCase 활성화
            //  현재 듀얼 라이트 기간: ProcessSellerSettlementService.process()에서 동기로 분개 처리 중
            //  recordLedgerEntryUseCase.recordPgSettlement(
            //          payload.settlementId(), payload.totalAllocatedAmount(), payload.pgFeeAmount()
            //  );
            //  long sellerSettlementDebit = payload.sellerPayoutAmount() + payload.platformFeeAmount();
            //  recordLedgerEntryUseCase.recordSellerSettlement(
            //          payload.settlementId(), sellerSettlementDebit,
            //          payload.sellerPayoutAmount(), payload.platformFeeAmount()
            //  );

            log.info("정산 실행 분개 이벤트 검증 완료 (듀얼 라이트). settlementId={}, sellerId={}, totalAllocated={}",
                    payload.settlementId(), payload.sellerId(), payload.totalAllocatedAmount());
        } catch (Exception e) {
            log.error("정산 실행 분개 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
