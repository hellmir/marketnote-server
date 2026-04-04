package com.personal.marketnote.common.outbox.entity;

import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.OutboxEventStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OutboxEventJpaEntity 행위 메서드 테스트")
class OutboxEventJpaEntityTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-15T10:00:00Z"), ZoneId.of("Asia/Seoul")
    );
    private static final LocalDateTime FIXED_NOW = LocalDateTime.now(FIXED_CLOCK);

    @Test
    @DisplayName("resetForRetry() 호출 시 status가 PENDING으로 retryCount가 0으로 변경된다")
    void resetForRetry_changesStatusToPendingAndResetsRetryCount() {
        // given
        OutboxEventJpaEntity entity = createFailedEntity();

        // when
        entity.resetForRetry();

        // then
        assertThat(entity.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(entity.getRetryCount()).isZero();
        assertThat(entity.getFailedAt()).isNull();
        assertThat(entity.getLastErrorMessage()).isNull();
    }

    @Test
    @DisplayName("discard() 호출 시 status가 DISCARDED로 discardReason과 discardedAt이 설정된다")
    void discard_changesStatusToDiscardedAndSetsReasonAndTime() {
        // given
        OutboxEventJpaEntity entity = createFailedEntity();
        String reason = "수동 폐기 처리";

        // when
        entity.discard(reason, FIXED_NOW);

        // then
        assertThat(entity.getStatus()).isEqualTo(OutboxEventStatus.DISCARDED);
        assertThat(entity.getDiscardReason()).isEqualTo(reason);
        assertThat(entity.getDiscardedAt()).isEqualTo(FIXED_NOW);
    }

    @Test
    @DisplayName("incrementRetry(errorMessage) 호출로 FAILED 전환 시 failedAt과 lastErrorMessage가 설정된다")
    void incrementRetry_setsFailedAtAndLastErrorMessageWhenFailed() {
        // given
        OutboxEventJpaEntity entity = createPendingEntity();
        for (int i = 0; i < 4; i++) {
            entity.incrementRetry("이전 에러", FIXED_NOW);
        }

        // when
        entity.incrementRetry("최종 에러 메시지", FIXED_NOW);

        // then
        assertThat(entity.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(entity.getFailedAt()).isEqualTo(FIXED_NOW);
        assertThat(entity.getLastErrorMessage()).isEqualTo("최종 에러 메시지");
    }

    private OutboxEventJpaEntity createPendingEntity() {
        OutboxEvent domainEvent = OutboxEvent.of(
                "event-id-1", "topic", "key", "TestEvent", "test-service", "{}", FIXED_CLOCK
        );
        return OutboxEventJpaEntity.from(domainEvent);
    }

    private OutboxEventJpaEntity createFailedEntity() {
        OutboxEventJpaEntity entity = createPendingEntity();
        for (int i = 0; i < 5; i++) {
            entity.incrementRetry("에러 " + i, FIXED_NOW);
        }
        return entity;
    }
}
