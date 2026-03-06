package com.personal.marketnote.user.adapter.out.event;

import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.UserReferralCompletedEvent;
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
class UserEventKafkaProducerReferralTest {
    @InjectMocks
    private UserEventKafkaProducer userEventKafkaProducer;

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
    @DisplayName("추천코드 등록 완료 이벤트 발행 시 올바른 토픽과 파티션 키로 전송된다")
    void publishUserReferralCompletedEvent_sendsToCorrectTopicWithRequestUserIdKey() {
        // given
        setUpClock("2026-03-02T10:00:00Z");
        when(kafkaTemplate.send(any(String.class), any(String.class), any()))
                .thenReturn(new CompletableFuture<>());

        // when
        userEventKafkaProducer.publishUserReferralCompletedEvent(1L, 2L);

        // then
        verify(kafkaTemplate).send(
                eq(KafkaTopicConstants.USER_REFERRAL_COMPLETED),
                eq("1"),
                any(EventEnvelope.class)
        );
    }

    @Test
    @DisplayName("추천코드 등록 완료 이벤트 발행 시 EventEnvelope에 올바른 페이로드가 포함된다")
    @SuppressWarnings("unchecked")
    void publishUserReferralCompletedEvent_envelopeContainsCorrectPayload() {
        // given
        setUpClock("2026-03-02T10:00:00Z");
        when(kafkaTemplate.send(any(String.class), any(String.class), any()))
                .thenReturn(new CompletableFuture<>());

        // when
        userEventKafkaProducer.publishUserReferralCompletedEvent(10L, 20L);

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(kafkaTemplate).send(
                eq(KafkaTopicConstants.USER_REFERRAL_COMPLETED),
                eq("10"),
                envelopeCaptor.capture()
        );

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.USER_REFERRAL_COMPLETED);
        assertThat(capturedEnvelope.source()).isEqualTo("user-service");
        assertThat(capturedEnvelope.eventId()).isNotNull();
        assertThat(capturedEnvelope.timestamp()).isNotNull();

        UserReferralCompletedEvent payload = (UserReferralCompletedEvent) capturedEnvelope.payload();
        assertThat(payload.requestUserId()).isEqualTo(10L);
        assertThat(payload.referredUserId()).isEqualTo(20L);
    }
}
