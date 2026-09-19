package com.personal.marketnote.commerce.domain.settlement;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.personal.marketnote.common.domain.money.Money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettlementPolicyTest {

    @Test
    @DisplayName("유효한 CreateState로 SettlementPolicy를 생성하면 ACTIVE 상태이다")
    void shouldCreateSettlementPolicyWithActiveStatus() {
        SettlementPolicy policy = SettlementPolicy.from(createValidState());

        assertThat(policy.isActive()).isTrue();
        assertThat(policy.getSellerId()).isEqualTo(1L);
        assertThat(policy.getPgFeeRate()).isEqualTo(350);
        assertThat(policy.getPlatformFeeRate()).isEqualTo(500);
        assertThat(policy.getSettlementCycle()).isEqualTo(SettlementCycle.MONTHLY);
        assertThat(policy.getMinPayoutAmount()).isEqualTo(Money.of(10000L));
    }

    @Test
    @DisplayName("PG 수수료율이 음수이면 InvalidSettlementPolicyException이 발생한다")
    void shouldThrowWhenPgFeeRateIsNegative() {
        SettlementPolicyCreateState state = SettlementPolicyCreateState.builder()
                .sellerId(1L)
                .pgFeeRate(-1)
                .platformFeeRate(500)
                .settlementCycle(SettlementCycle.MONTHLY)
                .minPayoutAmount(10000L)
                .build();

        assertThatThrownBy(() -> SettlementPolicy.from(state))
                .isInstanceOf(InvalidSettlementPolicyException.class);
    }

    @Test
    @DisplayName("플랫폼 수수료율이 10000을 초과하면 InvalidSettlementPolicyException이 발생한다")
    void shouldThrowWhenPlatformFeeRateExceedsBasisPoint() {
        SettlementPolicyCreateState state = SettlementPolicyCreateState.builder()
                .sellerId(1L)
                .pgFeeRate(350)
                .platformFeeRate(10001)
                .settlementCycle(SettlementCycle.MONTHLY)
                .minPayoutAmount(10000L)
                .build();

        assertThatThrownBy(() -> SettlementPolicy.from(state))
                .isInstanceOf(InvalidSettlementPolicyException.class);
    }

    @Test
    @DisplayName("수수료율 합계가 10000을 초과하면 InvalidSettlementPolicyException이 발생한다")
    void shouldThrowWhenFeeRateSumExceedsBasisPoint() {
        SettlementPolicyCreateState state = SettlementPolicyCreateState.builder()
                .sellerId(1L)
                .pgFeeRate(6000)
                .platformFeeRate(5000)
                .settlementCycle(SettlementCycle.MONTHLY)
                .minPayoutAmount(10000L)
                .build();

        assertThatThrownBy(() -> SettlementPolicy.from(state))
                .isInstanceOf(InvalidSettlementPolicyException.class);
    }

    @Test
    @DisplayName("최소 지급 금액이 음수이면 InvalidSettlementPolicyException이 발생한다")
    void shouldThrowWhenMinPayoutAmountIsNegative() {
        SettlementPolicyCreateState state = SettlementPolicyCreateState.builder()
                .sellerId(1L)
                .pgFeeRate(350)
                .platformFeeRate(500)
                .settlementCycle(SettlementCycle.MONTHLY)
                .minPayoutAmount(-1L)
                .build();

        assertThatThrownBy(() -> SettlementPolicy.from(state))
                .isInstanceOf(InvalidSettlementPolicyException.class);
    }

    @Test
    @DisplayName("SnapshotState로 SettlementPolicy를 복원하면 모든 필드가 그대로 매핑된다")
    void shouldRestoreFromSnapshotState() {
        SettlementPolicySnapshotState state = SettlementPolicySnapshotState.builder()
                .id(10L)
                .sellerId(1L)
                .pgFeeRate(350)
                .platformFeeRate(500)
                .settlementCycle(SettlementCycle.WEEKLY)
                .minPayoutAmount(5000L)
                .status(EntityStatus.INACTIVE)
                .build();

        SettlementPolicy policy = SettlementPolicy.from(state);

        assertThat(policy.getId()).isEqualTo(10L);
        assertThat(policy.isInactive()).isTrue();
        assertThat(policy.getSettlementCycle()).isEqualTo(SettlementCycle.WEEKLY);
    }

    @Test
    @DisplayName("update 시 유효한 값이면 필드가 변경된다")
    void shouldUpdateFieldsWithValidValues() {
        SettlementPolicy policy = SettlementPolicy.from(createValidState());

        policy.update(400, 600, SettlementCycle.BIWEEKLY, 20000L);

        assertThat(policy.getPgFeeRate()).isEqualTo(400);
        assertThat(policy.getPlatformFeeRate()).isEqualTo(600);
        assertThat(policy.getSettlementCycle()).isEqualTo(SettlementCycle.BIWEEKLY);
        assertThat(policy.getMinPayoutAmount()).isEqualTo(Money.of(20000L));
    }

    @Test
    @DisplayName("update 시 수수료율 합계가 10000을 초과하면 InvalidSettlementPolicyException이 발생한다")
    void shouldThrowWhenUpdateFeeRateSumExceedsBasisPoint() {
        SettlementPolicy policy = SettlementPolicy.from(createValidState());

        assertThatThrownBy(() -> policy.update(6000, 5000, SettlementCycle.MONTHLY, 10000L))
                .isInstanceOf(InvalidSettlementPolicyException.class);
    }

    @Test
    @DisplayName("deactivate를 호출하면 INACTIVE 상태로 변경된다")
    void shouldDeactivatePolicy() {
        SettlementPolicy policy = SettlementPolicy.from(createValidState());

        policy.deactivate();

        assertThat(policy.isInactive()).isTrue();
    }

    private SettlementPolicyCreateState createValidState() {
        return SettlementPolicyCreateState.builder()
                .sellerId(1L)
                .pgFeeRate(350)
                .platformFeeRate(500)
                .settlementCycle(SettlementCycle.MONTHLY)
                .minPayoutAmount(10000L)
                .build();
    }
}
