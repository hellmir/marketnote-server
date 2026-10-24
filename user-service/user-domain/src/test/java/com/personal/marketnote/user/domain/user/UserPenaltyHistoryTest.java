package com.personal.marketnote.user.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserPenaltyHistoryTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("UserPenaltyHistory.of로 생성 시 필드가 정확히 설정된다")
        void shouldCreateWithAllFields() {
            Long userId = 100L;
            int previousCount = 2;
            int currentCount = 3;
            String reason = "게시글 도배 행위";
            Long createdBy = 99L;

            UserPenaltyHistory history = UserPenaltyHistory.from(UserPenaltyHistoryCreateState.builder().userId(userId).previousCount(previousCount).currentCount(currentCount).reason(reason).createdBy(createdBy).build());

            assertThat(history.getUserId()).isEqualTo(userId);
            assertThat(history.getPreviousCount()).isEqualTo(previousCount);
            assertThat(history.getCurrentCount()).isEqualTo(currentCount);
            assertThat(history.getReason()).isEqualTo(reason);
            assertThat(history.getCreatedBy()).isEqualTo(createdBy);
        }

        @Test
        @DisplayName("UserPenaltyHistory.of로 생성 시 id와 createdAt은 null이다")
        void shouldNotSetIdAndCreatedAtOnCreate() {
            UserPenaltyHistory history = UserPenaltyHistory.from(UserPenaltyHistoryCreateState.builder().userId(1L).previousCount(0).currentCount(1).reason("이유").createdBy(2L).build());

            assertThat(history.getId()).isNull();
            assertThat(history.getCreatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("from(UserPenaltyHistorySnapshotState)")
    class FromSnapshot {

        @Test
        @DisplayName("SnapshotState로 복원 시 필드가 정확히 설정된다")
        void shouldRestoreAllFields() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 13, 10, 0);

            UserPenaltyHistory history = UserPenaltyHistory.from(
                    UserPenaltyHistorySnapshotState.builder()
                            .id(10L)
                            .userId(100L)
                            .previousCount(2)
                            .currentCount(3)
                            .reason("게시글 도배 행위")
                            .createdBy(99L)
                            .createdAt(now)
                            .build()
            );

            assertThat(history.getId()).isEqualTo(10L);
            assertThat(history.getUserId()).isEqualTo(100L);
            assertThat(history.getPreviousCount()).isEqualTo(2);
            assertThat(history.getCurrentCount()).isEqualTo(3);
            assertThat(history.getReason()).isEqualTo("게시글 도배 행위");
            assertThat(history.getCreatedBy()).isEqualTo(99L);
            assertThat(history.getCreatedAt()).isEqualTo(now);
        }
    }
}
