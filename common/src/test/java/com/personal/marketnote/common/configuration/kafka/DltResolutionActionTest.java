package com.personal.marketnote.common.configuration.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DltResolutionAction 테스트")
class DltResolutionActionTest {

    @Test
    @DisplayName("RETRY는 RETRIED 상태로 변환된다")
    void retry_toResolutionStatus_returnsRetried() {
        assertThat(DltResolutionAction.RETRY.toResolutionStatus())
                .isEqualTo(DltResolutionStatus.RETRIED);
    }

    @Test
    @DisplayName("DISCARD는 DISCARDED 상태로 변환된다")
    void discard_toResolutionStatus_returnsDiscarded() {
        assertThat(DltResolutionAction.DISCARD.toResolutionStatus())
                .isEqualTo(DltResolutionStatus.DISCARDED);
    }

    @Test
    @DisplayName("RETRY는 isRetry()가 true를 반환한다")
    void retry_isRetry_returnsTrue() {
        assertThat(DltResolutionAction.RETRY.isRetry()).isTrue();
    }

    @Test
    @DisplayName("DISCARD는 isRetry()가 false를 반환한다")
    void discard_isRetry_returnsFalse() {
        assertThat(DltResolutionAction.DISCARD.isRetry()).isFalse();
    }

    @Test
    @DisplayName("DISCARD는 isDiscard()가 true를 반환한다")
    void discard_isDiscard_returnsTrue() {
        assertThat(DltResolutionAction.DISCARD.isDiscard()).isTrue();
    }

    @Test
    @DisplayName("RETRY는 isDiscard()가 false를 반환한다")
    void retry_isDiscard_returnsFalse() {
        assertThat(DltResolutionAction.RETRY.isDiscard()).isFalse();
    }
}
