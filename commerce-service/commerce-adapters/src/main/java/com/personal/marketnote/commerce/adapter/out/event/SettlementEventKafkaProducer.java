package com.personal.marketnote.commerce.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.out.event.PublishSettlementEventPort;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.SettlementExecutedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class SettlementEventKafkaProducer implements PublishSettlementEventPort {
    private static final String SOURCE = "commerce-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publishSettlementExecutedEvent(Long settlementId, Long sellerId,
                                               Long totalAllocatedAmount, Long pgFeeAmount,
                                               Long platformFeeAmount, Long sellerPayoutAmount) {
        SettlementExecutedEvent payload = new SettlementExecutedEvent(
                settlementId, sellerId, totalAllocatedAmount,
                pgFeeAmount, platformFeeAmount, sellerPayoutAmount
        );
        String topic = KafkaTopicConstants.SETTLEMENT_EXECUTED;
        EventEnvelope<SettlementExecutedEvent> envelope = EventEnvelope.of(
                topic, SOURCE, payload, clock
        );

        try {
            String payloadJson = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = OutboxEvent.of(
                    envelope.eventId(), topic, settlementId.toString(),
                    envelope.eventType(), SOURCE, payloadJson, clock
            );
            saveOutboxEventPort.save(outboxEvent);
            log.info("Outbox 이벤트 저장. topic={}, settlementId={}, eventId={}",
                    topic, settlementId, envelope.eventId());
        } catch (Exception e) {
            log.error("Outbox 이벤트 저장 실패. topic={}, settlementId={}, error={}",
                    topic, settlementId, e.getMessage(), e);
        }
    }
}
