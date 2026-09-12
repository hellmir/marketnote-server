package com.personal.marketnote.reward.service.attendance;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import com.personal.marketnote.reward.domain.attendance.*;
import com.personal.marketnote.reward.port.in.result.attendance.GetAttendanceRelayStatusResult;
import com.personal.marketnote.reward.port.out.attendance.FindAttendancePolicyPort;
import com.personal.marketnote.reward.port.out.attendance.FindUserAttendanceHistoryPort;
import com.personal.marketnote.reward.port.out.attendance.FindUserAttendancePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAttendanceRelayStatusUseCase 테스트")
class GetAttendanceRelayStatusUseCaseTest {

    @InjectMocks
    private GetAttendanceRelayStatusService getAttendanceRelayStatusService;

    @Mock
    private FindUserAttendancePort findUserAttendancePort;

    @Mock
    private FindUserAttendanceHistoryPort findUserAttendanceHistoryPort;

    @Mock
    private FindAttendancePolicyPort findAttendancePolicyPort;

    private static final Long USER_ID = 1L;
    private static final Long ATTENDANCE_ID = 10L;

    @Nested
    @DisplayName("릴레이 현황 조회")
    class RelayStatusTest {

        @Test
        @DisplayName("출석 기록이 없을 때 릴레이 현황 조회 시 1일차 미완료 상태로 반환된다")
        void shouldReturnFirstDayIncompleteWhenNoAttendanceRecord() {
            // given
            LocalDate today = LocalDate.now();
            Year year = Year.from(today.getYear());
            Month month = Month.from(today.getMonthValue());

            when(findUserAttendancePort.findByUserIdAndYearAndMonth(USER_ID, year, month))
                    .thenReturn(Optional.empty());
            stubPoliciesForAllDays();

            // when
            GetAttendanceRelayStatusResult result = getAttendanceRelayStatusService.getRelayStatus(USER_ID);

            // then
            assertThat(result.currentRelayDay()).isEqualTo((short) 1);
            assertThat(result.todayChecked()).isFalse();
            assertThat(result.relaySlots()).hasSize(4);
            assertThat(result.relaySlots().get(0).completed()).isFalse();
            assertThat(result.relaySlots().get(1).completed()).isFalse();
            assertThat(result.relaySlots().get(2).completed()).isFalse();
            assertThat(result.relaySlots().get(3).completed()).isFalse();
        }

        @Test
        @DisplayName("1일차 출석 완료 후 릴레이 현황을 조회하면 1일차 완료로 표시된다")
        void shouldShowFirstDayCompletedAfterFirstDayAttendance() {
            // given
            LocalDate today = LocalDate.now();
            stubUserAttendance(today);
            stubTodayChecked(true);

            UserAttendanceHistory todayHistory = createHistory((short) 1, today.atTime(10, 0));
            when(findUserAttendanceHistoryPort.findLatestByUserAttendanceId(ATTENDANCE_ID))
                    .thenReturn(Optional.of(todayHistory));
            stubPoliciesForAllDays();

            // when
            GetAttendanceRelayStatusResult result = getAttendanceRelayStatusService.getRelayStatus(USER_ID);

            // then
            assertThat(result.currentRelayDay()).isEqualTo((short) 1);
            assertThat(result.todayChecked()).isTrue();
            assertThat(result.relaySlots().get(0).completed()).isTrue();
            assertThat(result.relaySlots().get(1).completed()).isFalse();
            assertThat(result.relaySlots().get(2).completed()).isFalse();
            assertThat(result.relaySlots().get(3).completed()).isFalse();
        }

        @Test
        @DisplayName("4일 전체 완료 후 릴레이 현황을 조회하면 새 사이클 1일차로 표시된다")
        void shouldShowNewCycleFirstDayAfterCompletingFullCycle() {
            // given
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            stubUserAttendance(today);
            stubTodayChecked(false);

            UserAttendanceHistory yesterdayHistory = createHistory((short) 4, yesterday.atTime(10, 0));
            when(findUserAttendanceHistoryPort.findLatestByUserAttendanceId(ATTENDANCE_ID))
                    .thenReturn(Optional.of(yesterdayHistory));
            stubPoliciesForAllDays();

            // when
            GetAttendanceRelayStatusResult result = getAttendanceRelayStatusService.getRelayStatus(USER_ID);

            // then
            assertThat(result.currentRelayDay()).isEqualTo((short) 1);
            assertThat(result.todayChecked()).isFalse();
            assertThat(result.relaySlots().get(0).completed()).isFalse();
        }

        @Test
        @DisplayName("todayChecked가 오늘 출석 여부를 정확히 반영한다")
        void shouldReflectTodayCheckedAccurately() {
            // given
            LocalDate today = LocalDate.now();
            stubUserAttendance(today);
            stubTodayChecked(true);

            UserAttendanceHistory todayHistory = createHistory((short) 2, today.atTime(10, 0));
            when(findUserAttendanceHistoryPort.findLatestByUserAttendanceId(ATTENDANCE_ID))
                    .thenReturn(Optional.of(todayHistory));
            stubPoliciesForAllDays();

            // when
            GetAttendanceRelayStatusResult result = getAttendanceRelayStatusService.getRelayStatus(USER_ID);

            // then
            assertThat(result.todayChecked()).isTrue();
            assertThat(result.currentRelayDay()).isEqualTo((short) 2);
            assertThat(result.relaySlots().get(0).completed()).isTrue();
            assertThat(result.relaySlots().get(1).completed()).isTrue();
            assertThat(result.relaySlots().get(2).completed()).isFalse();
            assertThat(result.relaySlots().get(3).completed()).isFalse();
        }

        @Test
        @DisplayName("relaySlots에 각 일차별 보상 정보가 정책과 일치한다")
        void shouldMatchRelaySlotRewardInfoWithPolicy() {
            // given
            LocalDate today = LocalDate.now();
            stubUserAttendance(today);
            stubTodayChecked(false);

            when(findUserAttendanceHistoryPort.findLatestByUserAttendanceId(ATTENDANCE_ID))
                    .thenReturn(Optional.empty());
            stubPoliciesForAllDays();

            // when
            GetAttendanceRelayStatusResult result = getAttendanceRelayStatusService.getRelayStatus(USER_ID);

            // then
            List<GetAttendanceRelayStatusResult.RelaySlot> slots = result.relaySlots();
            assertThat(slots).hasSize(4);

            assertThat(slots.get(0).day()).isEqualTo((short) 1);
            assertThat(slots.get(0).rewardType()).isEqualTo(AttendanceRewardType.POINT);
            assertThat(slots.get(0).rewardQuantity()).isEqualTo(50L);

            assertThat(slots.get(1).day()).isEqualTo((short) 2);
            assertThat(slots.get(1).rewardType()).isEqualTo(AttendanceRewardType.POINT);
            assertThat(slots.get(1).rewardQuantity()).isEqualTo(100L);

            assertThat(slots.get(2).day()).isEqualTo((short) 3);
            assertThat(slots.get(2).rewardType()).isEqualTo(AttendanceRewardType.POINT);
            assertThat(slots.get(2).rewardQuantity()).isEqualTo(150L);

            assertThat(slots.get(3).day()).isEqualTo((short) 4);
            assertThat(slots.get(3).rewardType()).isEqualTo(AttendanceRewardType.BOOSTER);
            assertThat(slots.get(3).rewardQuantity()).isEqualTo(1L);
        }

        private void stubUserAttendance(LocalDate today) {
            Year year = Year.from(today.getYear());
            Month month = Month.from(today.getMonthValue());

            UserAttendance attendance = UserAttendance.from(
                    UserAttendanceSnapshotState.builder()
                            .id(ATTENDANCE_ID)
                            .userId(USER_ID)
                            .year(year)
                            .month(month)
                            .totalRewardQuantity(100L)
                            .histories(List.of())
                            .build()
            );

            when(findUserAttendancePort.findByUserIdAndYearAndMonth(USER_ID, year, month))
                    .thenReturn(Optional.of(attendance));
        }

        private void stubTodayChecked(boolean checked) {
            when(findUserAttendanceHistoryPort.existsByUserAttendanceIdAndAttendedAtBetween(
                    eq(ATTENDANCE_ID), any(), any()
            )).thenReturn(checked);
        }

        private UserAttendanceHistory createHistory(short continuousPeriod, LocalDateTime attendedAt) {
            return UserAttendanceHistory.from(
                    UserAttendanceHistorySnapshotState.builder()
                            .id(99L)
                            .userAttendanceId(ATTENDANCE_ID)
                            .attendancePolicyId(continuousPeriod)
                            .continuousPeriod(continuousPeriod)
                            .rewardQuantity(50L)
                            .attendedAt(attendedAt)
                            .build()
            );
        }

        private void stubPoliciesForAllDays() {
            when(findAttendancePolicyPort.findByContinuousPeriodAndAttendenceDateIsNull((short) 1))
                    .thenReturn(Optional.of(createPolicy((short) 1, AttendanceRewardType.POINT, 50L)));
            when(findAttendancePolicyPort.findByContinuousPeriodAndAttendenceDateIsNull((short) 2))
                    .thenReturn(Optional.of(createPolicy((short) 2, AttendanceRewardType.POINT, 100L)));
            when(findAttendancePolicyPort.findByContinuousPeriodAndAttendenceDateIsNull((short) 3))
                    .thenReturn(Optional.of(createPolicy((short) 3, AttendanceRewardType.POINT, 150L)));
            when(findAttendancePolicyPort.findByContinuousPeriodAndAttendenceDateIsNull((short) 4))
                    .thenReturn(Optional.of(createPolicy((short) 4, AttendanceRewardType.BOOSTER, 1L)));
        }

        private AttendancePolicy createPolicy(short continuousPeriod, AttendanceRewardType rewardType, long rewardQuantity) {
            return AttendancePolicy.from(
                    AttendancePolicySnapshotState.builder()
                            .id(continuousPeriod)
                            .continuousPeriod(continuousPeriod)
                            .rewardType(rewardType)
                            .rewardQuantity(rewardQuantity)
                            .status(EntityStatus.ACTIVE)
                            .build()
            );
        }
    }
}
