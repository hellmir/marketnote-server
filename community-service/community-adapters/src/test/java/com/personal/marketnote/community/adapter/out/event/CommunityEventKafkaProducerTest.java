package com.personal.marketnote.community.adapter.out.event;

import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ReviewRegisteredEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityEventKafkaProducerTest {
    @InjectMocks
    private CommunityEventKafkaProducer communityEventKafkaProducer;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private Clock clock;

    private void setUpClock(String instant) {
        Clock fixedClock = Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Seoul"));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    @DisplayName("리뷰 등록 이벤트 발행 시 올바른 토픽과 파티션 키(orderId)로 전송된다")
    void publishReviewRegisteredEvent_sendsToCorrectTopicWithOrderIdKey() {
        // given
        setUpClock("2026-03-02T10:00:00Z");
        when(kafkaTemplate.send(any(String.class), any(String.class), any()))
                .thenReturn(new CompletableFuture<>());

        // when
        communityEventKafkaProducer.publishReviewRegisteredEvent(1L, 2L);

        // then
        verify(kafkaTemplate).send(
                eq(KafkaTopicConstants.REVIEW_REGISTERED),
                eq("1"),
                any(EventEnvelope.class)
        );
    }

    @Test
    @DisplayName("리뷰 등록 이벤트 발행 시 EventEnvelope에 올바른 페이로드(orderId, pricePolicyId)가 포함된다")
    @SuppressWarnings("unchecked")
    void publishReviewRegisteredEvent_envelopeContainsCorrectPayload() {
        // given
        setUpClock("2026-03-02T10:00:00Z");
        when(kafkaTemplate.send(any(String.class), any(String.class), any()))
                .thenReturn(new CompletableFuture<>());

        // when
        communityEventKafkaProducer.publishReviewRegisteredEvent(10L, 20L);

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(kafkaTemplate).send(
                eq(KafkaTopicConstants.REVIEW_REGISTERED),
                eq("10"),
                envelopeCaptor.capture()
        );

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.REVIEW_REGISTERED);
        assertThat(capturedEnvelope.source()).isEqualTo("community-service");
        assertThat(capturedEnvelope.eventId()).isNotNull();
        assertThat(capturedEnvelope.timestamp()).isNotNull();

        ReviewRegisteredEvent payload = (ReviewRegisteredEvent) capturedEnvelope.payload();
        assertThat(payload.orderId()).isEqualTo(10L);
        assertThat(payload.pricePolicyId()).isEqualTo(20L);
    }
}
