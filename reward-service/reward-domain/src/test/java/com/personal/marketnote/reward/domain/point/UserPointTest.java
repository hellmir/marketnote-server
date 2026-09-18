package com.personal.marketnote.reward.domain.point;

import com.personal.marketnote.reward.domain.exception.InsufficientPendingPointAmountException;
import com.personal.marketnote.reward.domain.exception.InvalidPointAmountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserPointTest {

    private static final Long USER_ID = 1L;
    private static final String USER_KEY = "user-key-001";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 3, 4, 10, 0);
    private static final LocalDateTime MODIFIED_AT = LocalDateTime.of(2026, 3, 4, 10, 0);

    private UserPoint createUserPoint(Long amount, Long addExpectedAmount) {
        return UserPoint.from(UserPointSnapshotState.builder()
                .userId(USER_ID)
                .userKey(USER_KEY)
                .amount(amount)
                .addExpectedAmount(addExpectedAmount)
                .expireExpectedAmount(100L)
                .createdAt(CREATED_AT)
                .modifiedAt(MODIFIED_AT)
                .build());
    }

    @Nested
    @DisplayName("from(UserPointCreateState)")
    class FromCreateState {

        @Test
        @DisplayName("CreateState로 UserPoint를 생성한다")
        void shouldCreateUserPointFromCreateState() {
            // given
            UserPointCreateState state = UserPointCreateState.builder()
                    .userId(USER_ID)
                    .userKey(USER_KEY)
                    .amount(1000L)
                    .addExpectedAmount(500L)
                    .expireExpectedAmount(200L)
                    .build();

            // when
            UserPoint userPoint = UserPoint.from(state);

            // then
            assertThat(userPoint.getUserId()).isEqualTo(USER_ID);
            assertThat(userPoint.getUserKey()).isEqualTo(USER_KEY);
            assertThat(userPoint.getAmountValue()).isEqualTo(1000L);
            assertThat(userPoint.getAddExpectedAmount()).isEqualTo(500L);
            assertThat(userPoint.getExpireExpectedAmount()).isEqualTo(200L);
            assertThat(userPoint.getCreatedAt()).isNull();
            assertThat(userPoint.getModifiedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("from(UserPointSnapshotState)")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로 UserPoint를 복원한다")
        void shouldRestoreUserPointFromSnapshotState() {
            // given
            UserPointSnapshotState state = UserPointSnapshotState.builder()
                    .userId(USER_ID)
                    .userKey(USER_KEY)
                    .amount(2000L)
                    .addExpectedAmount(300L)
                    .expireExpectedAmount(100L)
                    .createdAt(CREATED_AT)
                    .modifiedAt(MODIFIED_AT)
                    .build();

            // when
            UserPoint userPoint = UserPoint.from(state);

            // then
            assertThat(userPoint.getUserId()).isEqualTo(USER_ID);
            assertThat(userPoint.getUserKey()).isEqualTo(USER_KEY);
            assertThat(userPoint.getAmountValue()).isEqualTo(2000L);
            assertThat(userPoint.getAddExpectedAmount()).isEqualTo(300L);
            assertThat(userPoint.getExpireExpectedAmount()).isEqualTo(100L);
            assertThat(userPoint.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(userPoint.getModifiedAt()).isEqualTo(MODIFIED_AT);
        }
    }

    @Nested
    @DisplayName("changeAmount")
    class ChangeAmount {

        @Test
        @DisplayName("적립 시 포인트가 증가한다")
        void shouldIncreaseAmountWhenAccrual() {
            // given
            UserPoint userPoint = createUserPoint(1000L, 0L);

            // when
            userPoint.changeAmount(true, 500L);

            // then
            assertThat(userPoint.getAmountValue()).isEqualTo(1500L);
        }

        @Test
        @DisplayName("차감 시 포인트가 감소한다")
        void shouldDecreaseAmountWhenDeduction() {
            // given
            UserPoint userPoint = createUserPoint(1000L, 0L);

            // when
            userPoint.changeAmount(false, 300L);

            // then
            assertThat(userPoint.getAmountValue()).isEqualTo(700L);
        }

        @Test
        @DisplayName("차감 결과가 음수이면 InvalidPointAmountException이 발생한다")
        void shouldThrowWhenDeductionResultsInNegative() {
            // given
            UserPoint userPoint = createUserPoint(300L, 0L);

            // expect
            assertThatThrownBy(() -> userPoint.changeAmount(false, 500L))
                    .isInstanceOf(InvalidPointAmountException.class);
        }
    }

    @Nested
    @DisplayName("confirmPendingAmount")
    class ConfirmPendingAmount {

        @Test
        @DisplayName("예정 포인트를 차감하고 실제 포인트에 적립한다")
        void shouldDeductPendingAndAccrueActualAmount() {
            // given
            UserPoint userPoint = createUserPoint(1000L, 500L);

            // when
            userPoint.confirmPendingAmount(300L);

            // then
            assertThat(userPoint.getAddExpectedAmount()).isEqualTo(200L);
            assertThat(userPoint.getAmountValue()).isEqualTo(1300L);
        }

        @Test
        @DisplayName("예정 포인트 전액을 확정하면 예정 포인트가 0이 되고 실제 포인트에 전액 적립된다")
        void shouldConfirmAllPendingAmount() {
            // given
            UserPoint userPoint = createUserPoint(1000L, 500L);

            // when
            userPoint.confirmPendingAmount(500L);

            // then
            assertThat(userPoint.getAddExpectedAmount()).isEqualTo(0L);
            assertThat(userPoint.getAmountValue()).isEqualTo(1500L);
        }

        @Test
        @DisplayName("예정 포인트가 부족하면 InsufficientPendingPointAmountException이 발생한다")
        void shouldThrowWhenPendingAmountInsufficient() {
            // given
            UserPoint userPoint = createUserPoint(1000L, 200L);

            // expect
            assertThatThrownBy(() -> userPoint.confirmPendingAmount(500L))
                    .isInstanceOf(InsufficientPendingPointAmountException.class);
        }
    }

    @Nested
    @DisplayName("withAmount")
    class WithAmount {

        @Test
        @DisplayName("금액만 변경한 새 인스턴스를 반환하고 원본은 변경되지 않는다")
        void shouldReturnNewInstanceWithChangedAmountAndKeepOriginal() {
            // given
            UserPoint original = createUserPoint(1000L, 500L);

            // when
            UserPoint changed = original.withAmount(2000L);

            // then
            assertThat(changed).isNotSameAs(original);
            assertThat(changed.getAmountValue()).isEqualTo(2000L);
            assertThat(changed.getUserId()).isEqualTo(original.getUserId());
            assertThat(changed.getUserKey()).isEqualTo(original.getUserKey());
            assertThat(changed.getAddExpectedAmount()).isEqualTo(500L);
            assertThat(changed.getExpireExpectedAmount()).isEqualTo(original.getExpireExpectedAmount());
            assertThat(original.getAmountValue()).isEqualTo(1000L);
        }
    }
}
