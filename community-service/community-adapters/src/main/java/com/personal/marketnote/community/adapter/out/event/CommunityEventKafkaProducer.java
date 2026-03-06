package com.personal.marketnote.community.adapter.out.event;

import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ReviewRegisteredEvent;
import com.personal.marketnote.community.port.out.event.PublishReviewEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class CommunityEventKafkaProducer implements PublishReviewEventPort {
    private static final String SOURCE = "community-service";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Clock clock;

    @Override
    public void publishReviewRegisteredEvent(Long orderId, Long pricePolicyId) {
        ReviewRegisteredEvent payload = new ReviewRegisteredEvent(orderId, pricePolicyId);
        EventEnvelope<ReviewRegisteredEvent> envelope = EventEnvelope.of(
                KafkaTopicConstants.REVIEW_REGISTERED,
                SOURCE,
                payload,
                clock
        );

        // TODO: Kafka 단독 전환 시 발행 실패 처리 보강 필요 (Outbox 패턴 또는 동기 전환)
        kafkaTemplate.send(KafkaTopicConstants.REVIEW_REGISTERED, orderId.toString(), envelope)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka 이벤트 발행 실패. topic={}, orderId={}, pricePolicyId={}",
                                KafkaTopicConstants.REVIEW_REGISTERED, orderId, pricePolicyId, ex);
                    } else {
                        log.info("Kafka 이벤트 발행 성공. topic={}, orderId={}, offset={}",
                                KafkaTopicConstants.REVIEW_REGISTERED, orderId,
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
