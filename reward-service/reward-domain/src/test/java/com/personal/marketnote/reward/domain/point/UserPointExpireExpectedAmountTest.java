package com.personal.marketnote.reward.domain.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserPointExpireExpectedAmountTest {

    private static final Long USER_ID = 1L;
    private static final String USER_KEY = "user-key-001";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 13, 10, 0);

    @Test
    @DisplayName("CreateState에서 expireExpectedAmount를 PointAmount로 설정하면 동일 값이 유지된다")
    void shouldPreserveExpireExpectedAmountFromCreateState() {
        UserPointCreateState state = UserPointCreateState.builder()
                .userId(USER_ID)
                .userKey(USER_KEY)
                .amount(PointAmount.of(1000L))
                .addExpectedAmount(PointAmount.of(500L))
                .expireExpectedAmount(PointAmount.of(300L))
                .build();

        UserPoint userPoint = UserPoint.from(state);

        assertThat(userPoint.getExpireExpectedAmount()).isEqualTo(PointAmount.of(300L));
        assertThat(userPoint.getExpireExpectedAmountValue()).isEqualTo(300L);
    }

    @Test
    @DisplayName("SnapshotState에서 expireExpectedAmount를 PointAmount로 복원한다")
    void shouldRestoreExpireExpectedAmountFromSnapshotState() {
        UserPointSnapshotState state = UserPointSnapshotState.builder()
                .userId(USER_ID)
                .userKey(USER_KEY)
                .amount(PointAmount.of(2000L))
                .addExpectedAmount(PointAmount.of(100L))
                .expireExpectedAmount(PointAmount.of(700L))
                .createdAt(NOW)
                .modifiedAt(NOW)
                .build();

        UserPoint userPoint = UserPoint.from(state);

        assertThat(userPoint.getExpireExpectedAmount()).isEqualTo(PointAmount.of(700L));
        assertThat(userPoint.getExpireExpectedAmountValue()).isEqualTo(700L);
    }

    @Test
    @DisplayName("CreateState의 expireExpectedAmount가 null이면 PointAmount.zero()로 초기화된다")
    void shouldInitializeToZeroWhenNull() {
        UserPointCreateState state = UserPointCreateState.builder()
                .userId(USER_ID)
                .userKey(USER_KEY)
                .amount(PointAmount.of(1000L))
                .addExpectedAmount(PointAmount.of(500L))
                .expireExpectedAmount(null)
                .build();

        UserPoint userPoint = UserPoint.from(state);

        assertThat(userPoint.getExpireExpectedAmount()).isEqualTo(PointAmount.zero());
        assertThat(userPoint.getExpireExpectedAmount().isZero()).isTrue();
    }

    @Test
    @DisplayName("withAmount 호출 시 expireExpectedAmount는 변경되지 않는다")
    void shouldKeepExpireExpectedAmountWhenWithAmount() {
        UserPoint original = UserPoint.from(UserPointSnapshotState.builder()
                .userId(USER_ID)
                .userKey(USER_KEY)
                .amount(PointAmount.of(1000L))
                .addExpectedAmount(PointAmount.of(200L))
                .expireExpectedAmount(PointAmount.of(450L))
                .createdAt(NOW)
                .modifiedAt(NOW)
                .build());

        UserPoint changed = original.withAmount(5000L);

        assertThat(changed.getExpireExpectedAmount()).isEqualTo(PointAmount.of(450L));
        assertThat(changed.getExpireExpectedAmount()).isEqualTo(original.getExpireExpectedAmount());
    }
}
