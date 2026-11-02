package com.personal.marketnote.common.outbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OutboxResolutionAction Enum 테스트")
class OutboxResolutionActionTest {

    @Test
    @DisplayName("OutboxResolutionAction.RETRY의 isRetry()는 true를 반환한다")
    void retry_isRetryReturnsTrue() {
        assertThat(OutboxResolutionAction.RETRY.isRetry()).isTrue();
    }

    @Test
    @DisplayName("OutboxResolutionAction.DISCARD의 isDiscard()는 true를 반환한다")
    void discard_isDiscardReturnsTrue() {
        assertThat(OutboxResolutionAction.DISCARD.isDiscard()).isTrue();
    }

    @Test
    @DisplayName("RETRY의 isDiscard()는 false를 반환한다")
    void retry_isDiscardReturnsFalse() {
        assertThat(OutboxResolutionAction.RETRY.isDiscard()).isFalse();
    }

    @Test
    @DisplayName("DISCARD의 isRetry()는 false를 반환한다")
    void discard_isRetryReturnsFalse() {
        assertThat(OutboxResolutionAction.DISCARD.isRetry()).isFalse();
    }
}
