package com.personal.marketnote.community.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ReviewDeletedEvent;
import com.personal.marketnote.common.kafka.event.ReviewRegisteredEvent;
import com.personal.marketnote.common.kafka.event.ReviewUpdatedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityEventKafkaProducerTest {
    @InjectMocks
    private CommunityEventKafkaProducer communityEventKafkaProducer;

    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Clock clock;

    private void setUpClock(String instant) {
        Clock fixedClock = Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Seoul"));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    @DisplayName("리뷰 등록 이벤트 발행 시 올바른 토픽과 파티션 키(orderId)로 Outbox에 저장된다")
    void publishReviewRegisteredEvent_savesToOutboxWithCorrectTopicAndPartitionKey() throws Exception {
        // given
        setUpClock("2026-03-02T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        communityEventKafkaProducer.publishReviewRegisteredEvent(1L, 2L, 50L, 10, 4.5f);

        // then
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(outboxCaptor.capture());

        OutboxEvent captured = outboxCaptor.getValue();
        assertThat(captured.getTopic()).isEqualTo(KafkaTopicConstants.REVIEW_REGISTERED);
        assertThat(captured.getPartitionKey()).isEqualTo("1");
        assertThat(captured.getSource()).isEqualTo("community-service");
        assertThat(captured.getEventId()).isNotNull();
    }

    @Test
    @DisplayName("리뷰 등록 이벤트 발행 시 EventEnvelope에 올바른 페이로드가 포함된다")
    @SuppressWarnings("unchecked")
    void publishReviewRegisteredEvent_envelopeContainsCorrectPayload() throws Exception {
        // given
        setUpClock("2026-03-02T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        communityEventKafkaProducer.publishReviewRegisteredEvent(10L, 20L, 50L, 5, 4.2f);

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.REVIEW_REGISTERED);
        assertThat(capturedEnvelope.source()).isEqualTo("community-service");
        assertThat(capturedEnvelope.eventId()).isNotNull();
        assertThat(capturedEnvelope.timestamp()).isNotNull();

        ReviewRegisteredEvent payload = (ReviewRegisteredEvent) capturedEnvelope.payload();
        assertThat(payload.orderId()).isEqualTo(10L);
        assertThat(payload.pricePolicyId()).isEqualTo(20L);
        assertThat(payload.productId()).isEqualTo(50L);
        assertThat(payload.totalCount()).isEqualTo(5);
        assertThat(payload.averageRating()).isEqualTo(4.2f);
    }

    @Test
    @DisplayName("리뷰 수정 이벤트 발행 시 올바른 토픽과 파티션 키(productId)로 Outbox에 저장된다")
    void publishReviewUpdatedEvent_savesToOutboxWithCorrectTopicAndPartitionKey() throws Exception {
        // given
        setUpClock("2026-03-02T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        communityEventKafkaProducer.publishReviewUpdatedEvent(1L, 50L, 10, 4.5f);

        // then
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(outboxCaptor.capture());

        OutboxEvent captured = outboxCaptor.getValue();
        assertThat(captured.getTopic()).isEqualTo(KafkaTopicConstants.REVIEW_UPDATED);
        assertThat(captured.getPartitionKey()).isEqualTo("50");
        assertThat(captured.getSource()).isEqualTo("community-service");
        assertThat(captured.getEventId()).isNotNull();
    }

    @Test
    @DisplayName("리뷰 수정 이벤트 발행 시 EventEnvelope에 올바른 페이로드가 포함된다")
    @SuppressWarnings("unchecked")
    void publishReviewUpdatedEvent_envelopeContainsCorrectPayload() throws Exception {
        // given
        setUpClock("2026-03-02T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        communityEventKafkaProducer.publishReviewUpdatedEvent(1L, 50L, 8, 3.9f);

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.REVIEW_UPDATED);
        assertThat(capturedEnvelope.source()).isEqualTo("community-service");

        ReviewUpdatedEvent payload = (ReviewUpdatedEvent) capturedEnvelope.payload();
        assertThat(payload.reviewId()).isEqualTo(1L);
        assertThat(payload.productId()).isEqualTo(50L);
        assertThat(payload.totalCount()).isEqualTo(8);
        assertThat(payload.averageRating()).isEqualTo(3.9f);
    }

    @Test
    @DisplayName("리뷰 삭제 이벤트 발행 시 올바른 토픽과 파티션 키(productId)로 Outbox에 저장된다")
    void publishReviewDeletedEvent_savesToOutboxWithCorrectTopicAndPartitionKey() throws Exception {
        // given
        setUpClock("2026-03-02T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        communityEventKafkaProducer.publishReviewDeletedEvent(1L, 50L, 9, 4.1f);

        // then
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(outboxCaptor.capture());

        OutboxEvent captured = outboxCaptor.getValue();
        assertThat(captured.getTopic()).isEqualTo(KafkaTopicConstants.REVIEW_DELETED);
        assertThat(captured.getPartitionKey()).isEqualTo("50");
        assertThat(captured.getSource()).isEqualTo("community-service");
        assertThat(captured.getEventId()).isNotNull();
    }

    @Test
    @DisplayName("리뷰 삭제 이벤트 발행 시 EventEnvelope에 올바른 페이로드가 포함된다")
    @SuppressWarnings("unchecked")
    void publishReviewDeletedEvent_envelopeContainsCorrectPayload() throws Exception {
        // given
        setUpClock("2026-03-02T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        communityEventKafkaProducer.publishReviewDeletedEvent(1L, 50L, 7, 3.8f);

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.REVIEW_DELETED);
        assertThat(capturedEnvelope.source()).isEqualTo("community-service");

        ReviewDeletedEvent payload = (ReviewDeletedEvent) capturedEnvelope.payload();
        assertThat(payload.reviewId()).isEqualTo(1L);
        assertThat(payload.productId()).isEqualTo(50L);
        assertThat(payload.totalCount()).isEqualTo(7);
        assertThat(payload.averageRating()).isEqualTo(3.8f);
    }
}
