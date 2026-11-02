package com.personal.marketnote.notification.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SendStatusTest {

    @Nested
    @DisplayName("PENDING 상태")
    class Pending {

        @Test
        @DisplayName("isPending()이 true를 반환한다")
        void shouldReturnTrueForIsPending() {
            assertThat(SendStatus.PENDING.isPending()).isTrue();
        }

        @Test
        @DisplayName("isSent()가 false를 반환한다")
        void shouldReturnFalseForIsSent() {
            assertThat(SendStatus.PENDING.isSent()).isFalse();
        }
    }

    @Nested
    @DisplayName("SENT 상태")
    class Sent {

        @Test
        @DisplayName("isSent()가 true를 반환한다")
        void shouldReturnTrueForIsSent() {
            assertThat(SendStatus.SENT.isSent()).isTrue();
        }
    }

    @Nested
    @DisplayName("FAILED 상태")
    class Failed {

        @Test
        @DisplayName("isFailed()가 true를 반환한다")
        void shouldReturnTrueForIsFailed() {
            assertThat(SendStatus.FAILED.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("SKIPPED 상태")
    class Skipped {

        @Test
        @DisplayName("isSkipped()가 true를 반환한다")
        void shouldReturnTrueForIsSkipped() {
            assertThat(SendStatus.SKIPPED.isSkipped()).isTrue();
        }
    }

    @Nested
    @DisplayName("SCHEDULED 상태")
    class Scheduled {

        @Test
        @DisplayName("isScheduled()가 true를 반환한다")
        void shouldReturnTrueForIsScheduled() {
            assertThat(SendStatus.SCHEDULED.isScheduled()).isTrue();
        }
    }
}
