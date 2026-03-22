package com.personal.marketnote.common.configuration.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DltResolutionStatus 테스트")
class DltResolutionStatusTest {

    @Test
    @DisplayName("RETRIED는 isRetried()가 true를 반환한다")
    void retried_isRetried_returnsTrue() {
        assertThat(DltResolutionStatus.RETRIED.isRetried()).isTrue();
    }

    @Test
    @DisplayName("DISCARDED는 isRetried()가 false를 반환한다")
    void discarded_isRetried_returnsFalse() {
        assertThat(DltResolutionStatus.DISCARDED.isRetried()).isFalse();
    }

    @Test
    @DisplayName("DISCARDED는 isDiscarded()가 true를 반환한다")
    void discarded_isDiscarded_returnsTrue() {
        assertThat(DltResolutionStatus.DISCARDED.isDiscarded()).isTrue();
    }

    @Test
    @DisplayName("RETRIED는 isDiscarded()가 false를 반환한다")
    void retried_isDiscarded_returnsFalse() {
        assertThat(DltResolutionStatus.RETRIED.isDiscarded()).isFalse();
    }

    @Test
    @DisplayName("RETRIED는 isResolved()가 true를 반환한다")
    void retried_isResolved_returnsTrue() {
        assertThat(DltResolutionStatus.RETRIED.isResolved()).isTrue();
    }

    @Test
    @DisplayName("DISCARDED는 isResolved()가 true를 반환한다")
    void discarded_isResolved_returnsTrue() {
        assertThat(DltResolutionStatus.DISCARDED.isResolved()).isTrue();
    }
}
