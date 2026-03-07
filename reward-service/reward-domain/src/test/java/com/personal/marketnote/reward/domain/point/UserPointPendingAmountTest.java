package com.personal.marketnote.reward.domain.point;

import com.personal.marketnote.reward.domain.exception.InsufficientPendingPointAmountException;
import com.personal.marketnote.reward.domain.exception.InvalidPointAmountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserPointPendingAmountTest {

    private UserPoint createUserPoint(Long addExpectedAmount) {
        return UserPoint.from(UserPointSnapshotState.builder()
                .userId(1L)
                .amount(1000L)
                .addExpectedAmount(addExpectedAmount)
                .expireExpectedAmount(0L)
                .createdAt(LocalDateTime.of(2026, 3, 4, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 3, 4, 10, 0))
                .build());
    }

    @Nested
    @DisplayName("addPendingAmount")
    class AddPendingAmount {

        @Test
        @DisplayName("적립 예정 포인트를 추가하면 addExpectedAmount가 증가한다")
        void shouldIncreaseAddExpectedAmount() {
            // given
            UserPoint userPoint = createUserPoint(0L);

            // when
            userPoint.addPendingAmount(500L);

            // then
            assertThat(userPoint.getAddExpectedAmount()).isEqualTo(500L);
        }

        @Test
        @DisplayName("기존 적립 예정 포인트에 추가하면 누적된다")
        void shouldAccumulatePendingAmount() {
            // given
            UserPoint userPoint = createUserPoint(300L);

            // when
            userPoint.addPendingAmount(200L);

            // then
            assertThat(userPoint.getAddExpectedAmount()).isEqualTo(500L);
        }

        @Test
        @DisplayName("0 이하의 금액을 추가하면 InvalidPointAmountException이 발생한다")
        void shouldThrowWhenAmountIsZeroOrNegative() {
            // given
            UserPoint userPoint = createUserPoint(0L);

            // expect
            assertThatThrownBy(() -> userPoint.addPendingAmount(0L))
                    .isInstanceOf(InvalidPointAmountException.class);

            assertThatThrownBy(() -> userPoint.addPendingAmount(-100L))
                    .isInstanceOf(InvalidPointAmountException.class);
        }
    }

    @Nested
    @DisplayName("deductPendingAmount")
    class DeductPendingAmount {

        @Test
        @DisplayName("적립 예정 포인트를 차감하면 addExpectedAmount가 감소한다")
        void shouldDecreaseAddExpectedAmount() {
            // given
            UserPoint userPoint = createUserPoint(500L);

            // when
            userPoint.deductPendingAmount(300L);

            // then
            assertThat(userPoint.getAddExpectedAmount()).isEqualTo(200L);
        }

        @Test
        @DisplayName("적립 예정 포인트 전액을 차감하면 0이 된다")
        void shouldBecomeZeroWhenDeductingAll() {
            // given
            UserPoint userPoint = createUserPoint(500L);

            // when
            userPoint.deductPendingAmount(500L);

            // then
            assertThat(userPoint.getAddExpectedAmount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("적립 예정 포인트보다 큰 금액을 차감하면 InsufficientPendingPointAmountException이 발생한다")
        void shouldThrowWhenInsufficientPendingAmount() {
            // given
            UserPoint userPoint = createUserPoint(200L);

            // expect
            assertThatThrownBy(() -> userPoint.deductPendingAmount(500L))
                    .isInstanceOf(InsufficientPendingPointAmountException.class);
        }

        @Test
        @DisplayName("0 이하의 금액을 차감하면 InvalidPointAmountException이 발생한다")
        void shouldThrowWhenAmountIsZeroOrNegative() {
            // given
            UserPoint userPoint = createUserPoint(500L);

            // expect
            assertThatThrownBy(() -> userPoint.deductPendingAmount(0L))
                    .isInstanceOf(InvalidPointAmountException.class);
        }
    }

    @Nested
    @DisplayName("hasSufficientPendingAmount")
    class HasSufficientPendingAmount {

        @Test
        @DisplayName("적립 예정 포인트가 충분하면 true를 반환한다")
        void shouldReturnTrueWhenSufficient() {
            // given
            UserPoint userPoint = createUserPoint(500L);

            // expect
            assertThat(userPoint.hasSufficientPendingAmount(300L)).isTrue();
            assertThat(userPoint.hasSufficientPendingAmount(500L)).isTrue();
        }

        @Test
        @DisplayName("적립 예정 포인트가 부족하면 false를 반환한다")
        void shouldReturnFalseWhenInsufficient() {
            // given
            UserPoint userPoint = createUserPoint(200L);

            // expect
            assertThat(userPoint.hasSufficientPendingAmount(500L)).isFalse();
        }
    }
}
