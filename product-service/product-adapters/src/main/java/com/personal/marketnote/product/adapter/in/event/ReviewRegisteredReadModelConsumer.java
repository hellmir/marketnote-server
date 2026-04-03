package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ReviewRegisteredEvent;
import com.personal.marketnote.common.utility.FormatValidator;
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
public class ReviewRegisteredReadModelConsumer {
    private final ObjectMapper objectMapper;
    private final SaveReviewAggregateReadModelPort saveReviewAggregateReadModelPort;

    @KafkaListener(
            topics = KafkaTopicConstants.REVIEW_REGISTERED,
            groupId = "product-review-aggregate-read-model"
    )
    public void handleReviewRegisteredEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.REVIEW_REGISTERED)) {
            acknowledgment.acknowledge();
            return;
        }

        ReviewRegisteredEvent payload = envelope.getPayloadAs(ReviewRegisteredEvent.class, objectMapper);

        log.info("리뷰 등록 Read Model 이벤트 수신. eventId={}, productId={}, totalCount={}, averageRating={}",
                envelope.eventId(), payload.productId(), payload.totalCount(), payload.averageRating());

        if (FormatValidator.hasNoValue(payload.productId()) || payload.productId() <= 0) {
            log.warn("productId가 없는 기존 이벤트 무시. eventId={}", envelope.eventId());
            acknowledgment.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.totalCount()) || FormatValidator.hasNoValue(payload.averageRating())) {
            log.warn("집계 데이터가 없는 이벤트 무시. eventId={}", envelope.eventId());
            acknowledgment.acknowledge();
            return;
        }

        saveReviewAggregateReadModelPort.upsert(
                payload.productId(), payload.totalCount(), payload.averageRating()
        );

        log.info("리뷰 집계 Read Model 저장 완료. productId={}", payload.productId());
        acknowledgment.acknowledge();
    }
}
