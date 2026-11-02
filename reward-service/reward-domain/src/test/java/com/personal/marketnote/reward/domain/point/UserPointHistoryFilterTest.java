package com.personal.marketnote.reward.domain.point;

import com.personal.marketnote.reward.domain.exception.InvalidUserPointHistoryFilterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserPointHistoryFilterTest {

    @Test
    @DisplayName("ACCRUAL 필터는 toChangeType()에서 UserPointChangeType.ACCRUAL을 반환한다")
    void shouldReturnAccrualChangeType() {
        assertThat(UserPointHistoryFilter.ACCRUAL.toChangeType())
                .isEqualTo(UserPointChangeType.ACCRUAL);
    }

    @Test
    @DisplayName("DEDUCTION 필터는 toChangeType()에서 UserPointChangeType.DEDUCTION을 반환한다")
    void shouldReturnDeductionChangeType() {
        assertThat(UserPointHistoryFilter.DEDUCTION.toChangeType())
                .isEqualTo(UserPointChangeType.DEDUCTION);
    }

    @Test
    @DisplayName("ALL 필터는 toChangeType() 호출 시 도메인 예외를 던진다")
    void shouldThrowExceptionWhenFilterIsAll() {
        assertThatThrownBy(() -> UserPointHistoryFilter.ALL.toChangeType())
                .isInstanceOf(InvalidUserPointHistoryFilterException.class);
    }

    @Test
    @DisplayName("isAll은 ALL일 때만 true를 반환한다")
    void shouldReturnTrueForIsAllOnlyWhenAll() {
        assertThat(UserPointHistoryFilter.ALL.isAll()).isTrue();
        assertThat(UserPointHistoryFilter.ACCRUAL.isAll()).isFalse();
        assertThat(UserPointHistoryFilter.DEDUCTION.isAll()).isFalse();
    }
}
