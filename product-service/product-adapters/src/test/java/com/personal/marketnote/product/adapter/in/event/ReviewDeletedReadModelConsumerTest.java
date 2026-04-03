package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ReviewDeletedEvent;
import com.personal.marketnote.product.port.out.review.SaveReviewAggregateReadModelPort;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ReviewDeletedReadModelConsumerTest {

    @InjectMocks
    private ReviewDeletedReadModelConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SaveReviewAggregateReadModelPort saveReviewAggregateReadModelPort;

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(EventEnvelope<?> envelope) {
        return new ConsumerRecord<>(
                KafkaTopicConstants.REVIEW_DELETED, 0, 0, "1", envelope
        );
    }

    private EventEnvelope<ReviewDeletedEvent> createEnvelope(ReviewDeletedEvent payload) {
        return new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.REVIEW_DELETED,
                "community-service",
                LocalDateTime.now(Clock.fixed(Instant.parse("2026-03-31T10:00:00Z"), ZoneId.of("Asia/Seoul"))),
                payload
        );
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 Read Model을 upsert한다")
    void handleReviewDeletedEvent_validEvent_upsertsReadModel() {
        // given
        ReviewDeletedEvent payload = new ReviewDeletedEvent(1L, 50L, 9, 4.2f);
        EventEnvelope<ReviewDeletedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewDeletedEvent(record, acknowledgment);

        // then
        verify(saveReviewAggregateReadModelPort).upsert(50L, 9, 4.2f);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope가 null이면 즉시 acknowledge한다")
    void handleReviewDeletedEvent_nullEnvelope_acknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.REVIEW_DELETED, 0, 0, "1", null
        );

        // when
        consumer.handleReviewDeletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 타입이 불일치하면 즉시 acknowledge한다")
    void handleReviewDeletedEvent_eventTypeMismatch_acknowledges() {
        // given
        ReviewDeletedEvent payload = new ReviewDeletedEvent(1L, 50L, 9, 4.2f);
        EventEnvelope<ReviewDeletedEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                "wrong.event.type",
                "community-service",
                LocalDateTime.now(),
                payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewDeletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("reviewId가 유효하지 않으면 즉시 acknowledge한다")
    void handleReviewDeletedEvent_invalidReviewId_acknowledges() {
        // given
        ReviewDeletedEvent payload = new ReviewDeletedEvent(-1L, 50L, 9, 4.2f);
        EventEnvelope<ReviewDeletedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewDeletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 유효하지 않으면 즉시 acknowledge한다")
    void handleReviewDeletedEvent_invalidProductId_acknowledges() {
        // given
        ReviewDeletedEvent payload = new ReviewDeletedEvent(1L, 0L, 9, 4.2f);
        EventEnvelope<ReviewDeletedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewDeletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("reviewId가 null이면 즉시 acknowledge한다")
    void handleReviewDeletedEvent_nullReviewId_acknowledges() {
        // given
        ReviewDeletedEvent payload = new ReviewDeletedEvent(null, 50L, 9, 4.2f);
        EventEnvelope<ReviewDeletedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewDeletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 null이면 즉시 acknowledge한다")
    void handleReviewDeletedEvent_nullProductId_acknowledges() {
        // given
        ReviewDeletedEvent payload = new ReviewDeletedEvent(1L, null, 9, 4.2f);
        EventEnvelope<ReviewDeletedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewDeletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }
}
