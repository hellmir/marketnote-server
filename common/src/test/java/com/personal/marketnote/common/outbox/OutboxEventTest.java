package com.personal.marketnote.common.outbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OutboxEvent 도메인 테스트")
class OutboxEventTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-15T10:00:00Z"), ZoneId.of("Asia/Seoul")
    );

    @Test
    @DisplayName("of 메서드로 생성 시 PENDING 상태와 retryCount 0으로 초기화된다")
    void of_initializesPendingStatusAndZeroRetryCount() {
        // when
        OutboxEvent event = OutboxEvent.of(
                "event-id-1", "commerce.payment.approved", "order-123",
                "PaymentApproved", "commerce-service", "{\"orderId\":123}", FIXED_CLOCK
        );

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(event.getRetryCount()).isZero();
        assertThat(event.getMaxRetries()).isEqualTo(5);
        assertThat(event.getPublishedAt()).isNull();
    }

    @Test
    @DisplayName("of 메서드로 생성 시 Clock 기반 createdAt이 설정된다")
    void of_setsCreatedAtFromClock() {
        // when
        OutboxEvent event = OutboxEvent.of(
                "event-id-1", "commerce.payment.approved", "order-123",
                "PaymentApproved", "commerce-service", "{}", FIXED_CLOCK
        );

        // then
        LocalDateTime expectedCreatedAt = LocalDateTime.now(FIXED_CLOCK);
        assertThat(event.getCreatedAt()).isEqualTo(expectedCreatedAt);
    }

    @Test
    @DisplayName("of 메서드로 생성 시 전달된 필드가 올바르게 설정된다")
    void of_setsAllFields() {
        // given
        String eventId = "event-id-1";
        String topic = "commerce.payment.approved";
        String partitionKey = "order-123";
        String eventType = "PaymentApproved";
        String source = "commerce-service";
        String payload = "{\"orderId\":123}";

        // when
        OutboxEvent event = OutboxEvent.of(eventId, topic, partitionKey, eventType, source, payload, FIXED_CLOCK);

        // then
        assertThat(event.getEventId()).isEqualTo(eventId);
        assertThat(event.getTopic()).isEqualTo(topic);
        assertThat(event.getPartitionKey()).isEqualTo(partitionKey);
        assertThat(event.getEventType()).isEqualTo(eventType);
        assertThat(event.getSource()).isEqualTo(source);
        assertThat(event.getPayload()).isEqualTo(payload);
    }

    @Test
    @DisplayName("markPublished 호출 시 PUBLISHED 상태로 전환되고 publishedAt이 설정된다")
    void markPublished_changesStatusToPublishedAndSetsPublishedAt() {
        // given
        OutboxEvent event = OutboxEvent.of(
                "event-id-1", "topic", "key", "type", "source", "{}", FIXED_CLOCK
        );

        // when
        event.markPublished(FIXED_CLOCK);

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isEqualTo(LocalDateTime.now(FIXED_CLOCK));
    }

    @Test
    @DisplayName("incrementRetry 호출 시 retryCount가 1 증가한다")
    void incrementRetry_incrementsRetryCount() {
        // given
        OutboxEvent event = OutboxEvent.of(
                "event-id-1", "topic", "key", "type", "source", "{}", FIXED_CLOCK
        );

        // when
        event.incrementRetry();

        // then
        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
    }

    @Test
    @DisplayName("incrementRetry 호출 시 maxRetries에 도달하면 FAILED 상태로 전환된다")
    void incrementRetry_changesStatusToFailedWhenMaxRetriesReached() {
        // given
        OutboxEvent event = OutboxEvent.of(
                "event-id-1", "topic", "key", "type", "source", "{}", FIXED_CLOCK
        );

        // when
        for (int i = 0; i < 5; i++) {
            event.incrementRetry();
        }

        // then
        assertThat(event.getRetryCount()).isEqualTo(5);
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
    }

    @Test
    @DisplayName("isExhausted는 retryCount가 maxRetries 이상이면 true를 반환한다")
    void isExhausted_returnsTrueWhenRetryCountReachesMaxRetries() {
        // given
        OutboxEvent event = OutboxEvent.of(
                "event-id-1", "topic", "key", "type", "source", "{}", FIXED_CLOCK
        );
        for (int i = 0; i < 5; i++) {
            event.incrementRetry();
        }

        // when & then
        assertThat(event.isExhausted()).isTrue();
    }

    @Test
    @DisplayName("isExhausted는 retryCount가 maxRetries 미만이면 false를 반환한다")
    void isExhausted_returnsFalseWhenRetryCountBelowMaxRetries() {
        // given
        OutboxEvent event = OutboxEvent.of(
                "event-id-1", "topic", "key", "type", "source", "{}", FIXED_CLOCK
        );
        event.incrementRetry();

        // when & then
        assertThat(event.isExhausted()).isFalse();
    }
}
