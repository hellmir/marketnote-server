package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ReviewRegisteredEvent;
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
class ReviewRegisteredReadModelConsumerTest {

    @InjectMocks
    private ReviewRegisteredReadModelConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SaveReviewAggregateReadModelPort saveReviewAggregateReadModelPort;

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(EventEnvelope<?> envelope) {
        return new ConsumerRecord<>(
                KafkaTopicConstants.REVIEW_REGISTERED, 0, 0, "1", envelope
        );
    }

    private EventEnvelope<ReviewRegisteredEvent> createEnvelope(ReviewRegisteredEvent payload) {
        return new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.REVIEW_REGISTERED,
                "community-service",
                LocalDateTime.now(Clock.fixed(Instant.parse("2026-03-31T10:00:00Z"), ZoneId.of("Asia/Seoul"))),
                payload
        );
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 Read Model을 upsert한다")
    void handleReviewRegisteredEvent_validEvent_upsertsReadModel() {
        // given
        ReviewRegisteredEvent payload = new ReviewRegisteredEvent(1L, 2L, 50L, 10, 4.5f);
        EventEnvelope<ReviewRegisteredEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verify(saveReviewAggregateReadModelPort).upsert(50L, 10, 4.5f);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope가 null이면 즉시 acknowledge한다")
    void handleReviewRegisteredEvent_nullEnvelope_acknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.REVIEW_REGISTERED, 0, 0, "1", null
        );

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 타입이 불일치하면 즉시 acknowledge한다")
    void handleReviewRegisteredEvent_eventTypeMismatch_acknowledges() {
        // given
        ReviewRegisteredEvent payload = new ReviewRegisteredEvent(1L, 2L, 50L, 10, 4.5f);
        EventEnvelope<ReviewRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                "wrong.event.type",
                "community-service",
                LocalDateTime.now(),
                payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 null이면 기존 이벤트로 간주하고 무시한다")
    void handleReviewRegisteredEvent_nullProductId_acknowledges() {
        // given
        ReviewRegisteredEvent payload = new ReviewRegisteredEvent(1L, 2L, null, 10, 4.5f);
        EventEnvelope<ReviewRegisteredEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 0 이하이면 무시한다")
    void handleReviewRegisteredEvent_invalidProductId_acknowledges() {
        // given
        ReviewRegisteredEvent payload = new ReviewRegisteredEvent(1L, 2L, 0L, 10, 4.5f);
        EventEnvelope<ReviewRegisteredEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("totalCount가 null이면 무시한다")
    void handleReviewRegisteredEvent_nullTotalCount_acknowledges() {
        // given
        ReviewRegisteredEvent payload = new ReviewRegisteredEvent(1L, 2L, 50L, null, 4.5f);
        EventEnvelope<ReviewRegisteredEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("averageRating이 null이면 무시한다")
    void handleReviewRegisteredEvent_nullAverageRating_acknowledges() {
        // given
        ReviewRegisteredEvent payload = new ReviewRegisteredEvent(1L, 2L, 50L, 10, null);
        EventEnvelope<ReviewRegisteredEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleReviewRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveReviewAggregateReadModelPort);
        verify(acknowledgment).acknowledge();
    }
}
