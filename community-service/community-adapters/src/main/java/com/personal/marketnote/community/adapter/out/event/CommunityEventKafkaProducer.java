package com.personal.marketnote.community.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ReviewRegisteredEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.community.port.out.event.PublishReviewEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class CommunityEventKafkaProducer implements PublishReviewEventPort {
    private static final String SOURCE = "community-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publishReviewRegisteredEvent(Long orderId, Long pricePolicyId) {
        ReviewRegisteredEvent payload = new ReviewRegisteredEvent(orderId, pricePolicyId);
        String topic = KafkaTopicConstants.REVIEW_REGISTERED;
        EventEnvelope<ReviewRegisteredEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

        try {
            String payloadJson = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = OutboxEvent.of(
                    envelope.eventId(), topic, orderId.toString(),
                    envelope.eventType(), SOURCE, payloadJson, clock
            );
            saveOutboxEventPort.save(outboxEvent);
            log.info("Outbox 이벤트 저장. topic={}, orderId={}, eventId={}",
                    topic, orderId, envelope.eventId());
        } catch (Exception e) {
            log.error("Outbox 이벤트 저장 실패. topic={}, orderId={}, error={}",
                    topic, orderId, e.getMessage(), e);
        }
    }
}
