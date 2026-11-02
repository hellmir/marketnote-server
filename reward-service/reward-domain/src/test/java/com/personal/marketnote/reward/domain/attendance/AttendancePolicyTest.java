package com.personal.marketnote.reward.domain.attendance;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.reward.domain.exception.InvalidContinuousPeriodException;
import com.personal.marketnote.reward.domain.exception.InvalidRewardQuantityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttendancePolicyTest {

    private static final LocalDate ATTENDANCE_DATE = LocalDate.of(2026, 4, 11);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 11, 10, 0);

    @Test
    @DisplayName("CreateState로 출석 정책을 생성하면 필드가 올바르게 매핑된다")
    void shouldCreateAttendancePolicyFromCreateState() {
        // given
        AttendancePolicyCreateState state = AttendancePolicyCreateState.builder()
                .continuousPeriod(ContinuousPeriod.of((short) 7))
                .rewardType(AttendanceRewardType.POINT)
                .rewardQuantity(RewardQuantity.of(100L))
                .attendenceDate(ATTENDANCE_DATE)
                .build();

        // when
        AttendancePolicy policy = AttendancePolicy.from(state);

        // then
        assertThat(policy.getContinuousPeriodValue()).isEqualTo((short) 7);
        assertThat(policy.getRewardType()).isEqualTo(AttendanceRewardType.POINT);
        assertThat(policy.getRewardQuantityValue()).isEqualTo(100L);
        assertThat(policy.getAttendenceDate()).isEqualTo(ATTENDANCE_DATE);
    }

    @Test
    @DisplayName("SnapshotState로 ACTIVE 상태의 출석 정책을 복원하면 isActive가 true이다")
    void shouldRestoreActiveAttendancePolicyFromSnapshotState() {
        // given
        AttendancePolicy policy = createActivePolicy();

        // then
        assertThat(policy.isActive()).isTrue();
        assertThat(policy.isInactive()).isFalse();
        assertThat(policy.getId()).isEqualTo((short) 1);
    }

    @Test
    @DisplayName("delete 호출 시 상태가 INACTIVE로 변경된다")
    void shouldChangeStatusToInactiveWhenDeleted() {
        // given
        AttendancePolicy policy = createActivePolicy();
        assertThat(policy.isActive()).isTrue();

        // when
        policy.delete();

        // then
        assertThat(policy.isInactive()).isTrue();
        assertThat(policy.isActive()).isFalse();
    }

    @Test
    @DisplayName("ContinuousPeriod 최소값(1)로 정책을 생성할 수 있다")
    void shouldCreatePolicyWithMinContinuousPeriod() {
        AttendancePolicyCreateState state = AttendancePolicyCreateState.builder()
                .continuousPeriod(ContinuousPeriod.of((short) 1))
                .rewardType(AttendanceRewardType.POINT)
                .rewardQuantity(RewardQuantity.of(1L))
                .attendenceDate(ATTENDANCE_DATE)
                .build();

        AttendancePolicy policy = AttendancePolicy.from(state);

        assertThat(policy.getContinuousPeriodValue()).isEqualTo((short) 1);
    }

    @Test
    @DisplayName("ContinuousPeriod에 Short.MAX_VALUE까지 정책 생성이 허용된다")
    void shouldCreatePolicyWithMaxShortContinuousPeriod() {
        AttendancePolicyCreateState state = AttendancePolicyCreateState.builder()
                .continuousPeriod(ContinuousPeriod.of(Short.MAX_VALUE))
                .rewardType(AttendanceRewardType.POINT)
                .rewardQuantity(RewardQuantity.of(100L))
                .attendenceDate(ATTENDANCE_DATE)
                .build();

        AttendancePolicy policy = AttendancePolicy.from(state);

        assertThat(policy.getContinuousPeriodValue()).isEqualTo(Short.MAX_VALUE);
    }

    @Test
    @DisplayName("ContinuousPeriod가 0 이하이면 InvalidContinuousPeriodException을 던진다")
    void shouldThrowWhenContinuousPeriodZeroOrNegative() {
        assertThatThrownBy(() -> ContinuousPeriod.of((short) 0))
                .isInstanceOf(InvalidContinuousPeriodException.class);
        assertThatThrownBy(() -> ContinuousPeriod.of((short) -1))
                .isInstanceOf(InvalidContinuousPeriodException.class);
    }

    @Test
    @DisplayName("RewardQuantity 최소값(0)로 정책을 생성할 수 있다")
    void shouldCreatePolicyWithZeroRewardQuantity() {
        AttendancePolicyCreateState state = AttendancePolicyCreateState.builder()
                .continuousPeriod(ContinuousPeriod.of((short) 1))
                .rewardType(AttendanceRewardType.POINT)
                .rewardQuantity(RewardQuantity.of(0L))
                .attendenceDate(ATTENDANCE_DATE)
                .build();

        AttendancePolicy policy = AttendancePolicy.from(state);

        assertThat(policy.getRewardQuantityValue()).isEqualTo(0L);
    }

    @Test
    @DisplayName("RewardQuantity가 음수이면 InvalidRewardQuantityException을 던진다")
    void shouldThrowWhenRewardQuantityNegative() {
        assertThatThrownBy(() -> RewardQuantity.of(-1L))
                .isInstanceOf(InvalidRewardQuantityException.class);
    }

    @Test
    @DisplayName("withStatus는 상태만 변경된 새 인스턴스를 반환한다")
    void shouldReturnNewInstanceWithUpdatedStatus() {
        AttendancePolicy original = createActivePolicy();

        AttendancePolicy updated = original.withStatus(EntityStatus.INACTIVE);

        assertThat(updated).isNotSameAs(original);
        assertThat(updated.isInactive()).isTrue();
        assertThat(original.isActive()).isTrue();
        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getContinuousPeriodValue()).isEqualTo(original.getContinuousPeriodValue());
    }

    private AttendancePolicy createActivePolicy() {
        return AttendancePolicy.from(AttendancePolicySnapshotState.builder()
                .id((short) 1)
                .continuousPeriod(ContinuousPeriod.of((short) 7))
                .rewardType(AttendanceRewardType.POINT)
                .rewardQuantity(RewardQuantity.of(100L))
                .attendenceDate(ATTENDANCE_DATE)
                .status(EntityStatus.ACTIVE)
                .createdAt(NOW)
                .modifiedAt(NOW)
                .build()
        );
    }
}
