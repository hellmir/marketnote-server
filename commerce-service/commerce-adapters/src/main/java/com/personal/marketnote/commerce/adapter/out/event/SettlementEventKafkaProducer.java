package com.personal.marketnote.commerce.adapter.out.event;

import com.personal.marketnote.commerce.port.out.event.PublishSettlementEventPort;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.SettlementExecutedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class SettlementEventKafkaProducer implements PublishSettlementEventPort {
    private static final String SOURCE = "commerce-service";

    private final KafkaTemplate<String, Object> kafkaTemplate;
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

        kafkaTemplate.send(topic, settlementId.toString(), envelope)
                .whenComplete((result, ex) -> {
                    if (FormatValidator.hasValue(ex)) {
                        log.error("Kafka 이벤트 발행 실패. topic={}, settlementId={}, sellerId={}",
                                topic, settlementId, sellerId, ex);
                        return;
                    }

                    log.info("Kafka 이벤트 발행 성공. topic={}, settlementId={}, sellerId={}, offset={}",
                            topic, settlementId, sellerId,
                            result.getRecordMetadata().offset());
                });
    }
}
