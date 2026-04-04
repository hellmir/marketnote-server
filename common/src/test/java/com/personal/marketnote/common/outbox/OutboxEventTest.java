package com.personal.marketnote.common.outbox;

import com.personal.marketnote.common.outbox.exception.InvalidOutboxEventStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        event.incrementRetry("테스트 에러", FIXED_CLOCK);

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
            event.incrementRetry("테스트 에러", FIXED_CLOCK);
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
            event.incrementRetry("테스트 에러", FIXED_CLOCK);
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
        event.incrementRetry("테스트 에러", FIXED_CLOCK);

        // when & then
        assertThat(event.isExhausted()).isFalse();
    }

    @Test
    @DisplayName("FAILED 상태에서 resetForRetry() 호출 시 PENDING으로 전이되고 retryCount가 0으로 리셋된다")
    void resetForRetry_changesFailedToPendingAndResetsRetryCount() {
        // given
        OutboxEvent event = createFailedEvent();

        // when
        event.resetForRetry();

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(event.getRetryCount()).isZero();
        assertThat(event.getFailedAt()).isNull();
        assertThat(event.getLastErrorMessage()).isNull();
    }

    @Test
    @DisplayName("FAILED 상태에서 discard() 호출 시 DISCARDED로 전이되고 discardReason과 discardedAt이 설정된다")
    void discard_changesFailedToDiscardedAndSetsReasonAndTime() {
        // given
        OutboxEvent event = createFailedEvent();
        String reason = "수동 폐기 처리";

        // when
        event.discard(reason, FIXED_CLOCK);

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.DISCARDED);
        assertThat(event.getDiscardReason()).isEqualTo(reason);
        assertThat(event.getDiscardedAt()).isEqualTo(LocalDateTime.now(FIXED_CLOCK));
    }

    @Test
    @DisplayName("PENDING 상태에서 resetForRetry() 호출 시 InvalidOutboxEventStatusTransitionException이 발생한다")
    void resetForRetry_throwsExceptionWhenPending() {
        // given
        OutboxEvent event = OutboxEvent.of(
                "event-id-1", "topic", "key", "type", "source", "{}", FIXED_CLOCK
        );

        // when & then
        assertThatThrownBy(event::resetForRetry)
                .isInstanceOf(InvalidOutboxEventStatusTransitionException.class);
    }

    @Test
    @DisplayName("PUBLISHED 상태에서 discard() 호출 시 InvalidOutboxEventStatusTransitionException이 발생한다")
    void discard_throwsExceptionWhenPublished() {
        // given
        OutboxEvent event = OutboxEvent.of(
                "event-id-1", "topic", "key", "type", "source", "{}", FIXED_CLOCK
        );
        event.markPublished(FIXED_CLOCK);

        // when & then
        assertThatThrownBy(() -> event.discard("사유", FIXED_CLOCK))
                .isInstanceOf(InvalidOutboxEventStatusTransitionException.class);
    }

    @Test
    @DisplayName("incrementRetry(errorMessage) 호출로 FAILED 전환 시 failedAt과 lastErrorMessage가 설정된다")
    void incrementRetry_setsFailedAtAndLastErrorMessageWhenFailed() {
        // given
        OutboxEvent event = OutboxEvent.of(
                "event-id-1", "topic", "key", "type", "source", "{}", FIXED_CLOCK
        );
        for (int i = 0; i < 4; i++) {
            event.incrementRetry("이전 에러", FIXED_CLOCK);
        }

        // when
        event.incrementRetry("최종 에러 메시지", FIXED_CLOCK);

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.getFailedAt()).isEqualTo(LocalDateTime.now(FIXED_CLOCK));
        assertThat(event.getLastErrorMessage()).isEqualTo("최종 에러 메시지");
    }

    @Test
    @DisplayName("incrementRetry(errorMessage) 호출 시 retryCount가 증가하고 lastErrorMessage가 최신 에러로 갱신된다")
    void incrementRetry_incrementsRetryCountAndUpdatesLastErrorMessage() {
        // given
        OutboxEvent event = OutboxEvent.of(
                "event-id-1", "topic", "key", "type", "source", "{}", FIXED_CLOCK
        );

        // when
        event.incrementRetry("첫 번째 에러", FIXED_CLOCK);
        event.incrementRetry("두 번째 에러", FIXED_CLOCK);

        // then
        assertThat(event.getRetryCount()).isEqualTo(2);
        assertThat(event.getLastErrorMessage()).isEqualTo("두 번째 에러");
    }

    private OutboxEvent createFailedEvent() {
        OutboxEvent event = OutboxEvent.of(
                "event-id-1", "topic", "key", "type", "source", "{}", FIXED_CLOCK
        );
        for (int i = 0; i < 5; i++) {
            event.incrementRetry("에러 " + i, FIXED_CLOCK);
        }
        return event;
    }
}
