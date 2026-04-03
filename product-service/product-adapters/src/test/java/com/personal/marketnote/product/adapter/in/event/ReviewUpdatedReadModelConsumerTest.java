package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ReviewUpdatedEvent;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewUpdatedReadModelConsumerTest {

    @InjectMocks
    private ReviewUpdatedReadModelConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SaveReviewAggregateReadModelPort saveReviewAggregateReadModelPort;

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(EventEnvelope<?> envelope) {
        return new ConsumerRecord<>(
                KafkaTopicConstants.REVIEW_UPDATED, 0, 0, "1", envelope
        );
    }

    private EventEnvelope<ReviewUpdatedEvent> createEnvelope(ReviewUpdatedEvent payload) {
        return new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.REVIEW_UPDATED,
                "community-service",
                LocalDateTime.now(Clock.fixed(Instant.parse("2026-03-31T10:00:00Z"), ZoneId.of("Asia/Seoul"))),
                payload
        );
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 Read Model을 upsert한다")
    void handleReviewUpdatedEvent_validEvent_upsertsReadModel() {
        // given
        ReviewUpdatedEvent payload = new ReviewUpdatedEvent(1L, 50L, 10, 4.5f);
        EventEnvelope<ReviewUpdatedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewUpdatedEvent(record, acknowledgment);

        // then
        verify(saveReviewAggregateReadModelPort).upsert(50L, 10, 4.5f);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope가 null이면 즉시 acknowledge한다")
    void handleReviewUpdatedEvent_nullEnvelope_acknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.REVIEW_UPDATED, 0, 0, "1", null
        );

        // when
        consumer.handleReviewUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 타입이 불일치하면 즉시 acknowledge한다")
    void handleReviewUpdatedEvent_eventTypeMismatch_acknowledges() {
        // given
        ReviewUpdatedEvent payload = new ReviewUpdatedEvent(1L, 50L, 10, 4.5f);
        EventEnvelope<ReviewUpdatedEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                "wrong.event.type",
                "community-service",
                LocalDateTime.now(),
                payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("reviewId가 유효하지 않으면 즉시 acknowledge한다")
    void handleReviewUpdatedEvent_invalidReviewId_acknowledges() {
        // given
        ReviewUpdatedEvent payload = new ReviewUpdatedEvent(-1L, 50L, 10, 4.5f);
        EventEnvelope<ReviewUpdatedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 유효하지 않으면 즉시 acknowledge한다")
    void handleReviewUpdatedEvent_invalidProductId_acknowledges() {
        // given
        ReviewUpdatedEvent payload = new ReviewUpdatedEvent(1L, 0L, 10, 4.5f);
        EventEnvelope<ReviewUpdatedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("reviewId가 null이면 즉시 acknowledge한다")
    void handleReviewUpdatedEvent_nullReviewId_acknowledges() {
        // given
        ReviewUpdatedEvent payload = new ReviewUpdatedEvent(null, 50L, 10, 4.5f);
        EventEnvelope<ReviewUpdatedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 null이면 즉시 acknowledge한다")
    void handleReviewUpdatedEvent_nullProductId_acknowledges() {
        // given
        ReviewUpdatedEvent payload = new ReviewUpdatedEvent(1L, null, 10, 4.5f);
        EventEnvelope<ReviewUpdatedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }
}
