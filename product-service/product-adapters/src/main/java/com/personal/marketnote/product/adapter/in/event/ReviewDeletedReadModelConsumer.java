package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ReviewDeletedEvent;
import com.personal.marketnote.product.port.out.review.SaveReviewAggregateReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewDeletedReadModelConsumer {
    private final ObjectMapper objectMapper;
    private final SaveReviewAggregateReadModelPort saveReviewAggregateReadModelPort;

    @KafkaListener(
            topics = KafkaTopicConstants.REVIEW_DELETED,
            groupId = "product-review-aggregate-read-model"
    )
    public void handleReviewDeletedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.REVIEW_DELETED)) {
            acknowledgment.acknowledge();
            return;
        }

        ReviewDeletedEvent payload = envelope.getPayloadAs(ReviewDeletedEvent.class, objectMapper);

        log.info("리뷰 삭제 Read Model 이벤트 수신. eventId={}, reviewId={}, productId={}, totalCount={}, averageRating={}",
                envelope.eventId(), payload.reviewId(), payload.productId(),
                payload.totalCount(), payload.averageRating());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("reviewId", payload.reviewId()),
                EventPayloadValidator.id("productId", payload.productId()))) {
            acknowledgment.acknowledge();
            return;
        }

        saveReviewAggregateReadModelPort.upsert(
                payload.productId(), payload.totalCount(), payload.averageRating()
        );

        log.info("리뷰 집계 Read Model 업데이트 완료 (삭제 반영). productId={}", payload.productId());
        acknowledgment.acknowledge();
    }
}
