package com.personal.marketnote.community.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventRegisteredEvent;
import com.personal.marketnote.common.kafka.event.InquiryAnsweredEvent;
import com.personal.marketnote.common.kafka.event.NoticeRegisteredEvent;
import com.personal.marketnote.common.kafka.event.ReviewDeletedEvent;
import com.personal.marketnote.common.kafka.event.ReviewRegisteredEvent;
import com.personal.marketnote.common.kafka.event.ReviewUpdatedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.community.port.out.event.PublishPostEventPort;
import com.personal.marketnote.community.port.out.event.PublishReviewEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class CommunityEventKafkaProducer implements PublishReviewEventPort, PublishPostEventPort {
    private static final String SOURCE = "community-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publishReviewRegisteredEvent(Long orderId, Long pricePolicyId, Long productId, Integer totalCount, Float averageRating) {
        ReviewRegisteredEvent payload = new ReviewRegisteredEvent(orderId, pricePolicyId, productId, totalCount, averageRating);
        String topic = KafkaTopicConstants.REVIEW_REGISTERED;
        EventEnvelope<ReviewRegisteredEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

        saveOutboxEvent(topic, orderId.toString(), envelope);
    }

    @Override
    public void publishReviewUpdatedEvent(Long reviewId, Long productId, Integer totalCount, Float averageRating) {
        ReviewUpdatedEvent payload = new ReviewUpdatedEvent(reviewId, productId, totalCount, averageRating);
        String topic = KafkaTopicConstants.REVIEW_UPDATED;
        EventEnvelope<ReviewUpdatedEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

        saveOutboxEvent(topic, productId.toString(), envelope);
    }

    @Override
    public void publishReviewDeletedEvent(Long reviewId, Long productId, Integer totalCount, Float averageRating) {
        ReviewDeletedEvent payload = new ReviewDeletedEvent(reviewId, productId, totalCount, averageRating);
        String topic = KafkaTopicConstants.REVIEW_DELETED;
        EventEnvelope<ReviewDeletedEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

        saveOutboxEvent(topic, productId.toString(), envelope);
    }

    @Override
    public void publishNoticeRegisteredEvent(Long postId, String title) {
        NoticeRegisteredEvent payload = new NoticeRegisteredEvent(postId, title);
        String topic = KafkaTopicConstants.NOTICE_REGISTERED;
        EventEnvelope<NoticeRegisteredEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

        saveOutboxEvent(topic, postId.toString(), envelope);
    }

    @Override
    public void publishEventRegisteredEvent(Long postId, String title) {
        EventRegisteredEvent payload = new EventRegisteredEvent(postId, title);
        String topic = KafkaTopicConstants.EVENT_REGISTERED;
        EventEnvelope<EventRegisteredEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

        saveOutboxEvent(topic, postId.toString(), envelope);
    }

    @Override
    public void publishInquiryAnsweredEvent(Long userId, Long postId, String title, String board) {
        InquiryAnsweredEvent payload = new InquiryAnsweredEvent(userId, postId, title, board);
        String topic = KafkaTopicConstants.INQUIRY_ANSWERED;
        EventEnvelope<InquiryAnsweredEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

        saveOutboxEvent(topic, postId.toString(), envelope);
    }

    private <T> void saveOutboxEvent(String topic, String partitionKey, EventEnvelope<T> envelope) {
        try {
            String payloadJson = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = OutboxEvent.of(
                    envelope.eventId(), topic, partitionKey,
                    envelope.eventType(), SOURCE, payloadJson, clock
            );
            saveOutboxEventPort.save(outboxEvent);
            log.info("Outbox 이벤트 저장. topic={}, partitionKey={}, eventId={}",
                    topic, partitionKey, envelope.eventId());
        } catch (Exception e) {
            log.error("Outbox 이벤트 저장 실패. topic={}, partitionKey={}, error={}",
                    topic, partitionKey, e.getMessage(), e);
        }
    }
}
