package com.personal.marketnote.reward.domain.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPointChangeTypeTest {

    @Test
    @DisplayName("ACCRUAL이면 isAccrual은 true, isDeduction은 false를 반환한다")
    void shouldReturnTrueForAccrualWhenTypeIsAccrual() {
        UserPointChangeType type = UserPointChangeType.ACCRUAL;

        assertThat(type.isAccrual()).isTrue();
        assertThat(type.isDeduction()).isFalse();
    }

    @Test
    @DisplayName("DEDUCTION이면 isDeduction은 true, isAccrual은 false를 반환한다")
    void shouldReturnTrueForDeductionWhenTypeIsDeduction() {
        UserPointChangeType type = UserPointChangeType.DEDUCTION;

        assertThat(type.isDeduction()).isTrue();
        assertThat(type.isAccrual()).isFalse();
    }
}
