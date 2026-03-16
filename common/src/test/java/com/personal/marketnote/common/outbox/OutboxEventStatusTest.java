package com.personal.marketnote.common.outbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OutboxEventStatus Enum 테스트")
class OutboxEventStatusTest {

    @Test
    @DisplayName("PENDING 상태의 isPending은 true를 반환한다")
    void pending_isPendingReturnsTrue() {
        assertThat(OutboxEventStatus.PENDING.isPending()).isTrue();
    }

    @Test
    @DisplayName("PUBLISHED 상태의 isPublished는 true를 반환한다")
    void published_isPublishedReturnsTrue() {
        assertThat(OutboxEventStatus.PUBLISHED.isPublished()).isTrue();
    }

    @Test
    @DisplayName("FAILED 상태의 isFailed는 true를 반환한다")
    void failed_isFailedReturnsTrue() {
        assertThat(OutboxEventStatus.FAILED.isFailed()).isTrue();
    }

    @Test
    @DisplayName("PENDING 상태의 isPublished와 isFailed는 false를 반환한다")
    void pending_otherPredicatesReturnFalse() {
        assertThat(OutboxEventStatus.PENDING.isPublished()).isFalse();
        assertThat(OutboxEventStatus.PENDING.isFailed()).isFalse();
    }

    @Test
    @DisplayName("PUBLISHED 상태의 isPending과 isFailed는 false를 반환한다")
    void published_otherPredicatesReturnFalse() {
        assertThat(OutboxEventStatus.PUBLISHED.isPending()).isFalse();
        assertThat(OutboxEventStatus.PUBLISHED.isFailed()).isFalse();
    }

    @Test
    @DisplayName("FAILED 상태의 isPending과 isPublished는 false를 반환한다")
    void failed_otherPredicatesReturnFalse() {
        assertThat(OutboxEventStatus.FAILED.isPending()).isFalse();
        assertThat(OutboxEventStatus.FAILED.isPublished()).isFalse();
    }
}
