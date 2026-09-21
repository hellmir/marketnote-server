package com.personal.marketnote.reward.domain.attendance;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

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
                .build());
    }
}
