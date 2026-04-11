package com.personal.marketnote.reward.service.attendance;

import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import com.personal.marketnote.reward.domain.attendance.UserAttendance;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistory;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceSnapshotState;
import com.personal.marketnote.reward.port.in.command.attendance.GetMonthlyAttendanceQuery;
import com.personal.marketnote.reward.port.in.result.attendance.GetMonthlyAttendanceResult;
import com.personal.marketnote.reward.port.out.attendance.FindUserAttendancePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMonthlyAttendanceUseCase 테스트")
class GetMonthlyAttendanceUseCaseTest {

    @InjectMocks
    private GetMonthlyAttendanceService getMonthlyAttendanceService;

    @Mock
    private FindUserAttendancePort findUserAttendancePort;

    @Test
    @DisplayName("월별 출석 이력이 존재하면 출석 날짜 목록과 보상 합계를 반환한다")
    void shouldReturnMonthlyAttendanceWithHistories() {
        // given
        Long userId = 1L;
        Year year = Year.from(2026);
        Month month = Month.from(4);
        GetMonthlyAttendanceQuery query = GetMonthlyAttendanceQuery.of(userId, 2026, 4);

        UserAttendance attendance = UserAttendance.from(
                UserAttendanceSnapshotState.builder()
                        .id(1L)
                        .userId(userId)
                        .year(year)
                        .month(month)
                        .totalRewardQuantity(300L)
                        .histories(List.of(
                                createHistory(LocalDateTime.of(2026, 4, 1, 9, 0)),
                                createHistory(LocalDateTime.of(2026, 4, 2, 9, 0))
                        ))
                        .build()
        );

        when(findUserAttendancePort.findByUserIdAndYearAndMonth(userId, year, month))
                .thenReturn(Optional.of(attendance));

        // when
        GetMonthlyAttendanceResult result = getMonthlyAttendanceService.getMonthlyAttendanceHistories(query);

        // then
        assertThat(result.totalAttendanceDays()).isEqualTo(2);
        assertThat(result.totalRewardQuantity()).isEqualTo(300L);
        assertThat(result.attendanceDates()).hasSize(2);
        verify(findUserAttendancePort).findByUserIdAndYearAndMonth(userId, year, month);
    }

    @Test
    @DisplayName("월별 출석 이력이 없으면 빈 결과를 반환한다")
    void shouldReturnEmptyResultWhenNoAttendance() {
        // given
        Long userId = 1L;
        Year year = Year.from(2026);
        Month month = Month.from(4);
        GetMonthlyAttendanceQuery query = GetMonthlyAttendanceQuery.of(userId, 2026, 4);

        when(findUserAttendancePort.findByUserIdAndYearAndMonth(userId, year, month))
                .thenReturn(Optional.empty());

        // when
        GetMonthlyAttendanceResult result = getMonthlyAttendanceService.getMonthlyAttendanceHistories(query);

        // then
        assertThat(result.totalAttendanceDays()).isZero();
        assertThat(result.totalRewardQuantity()).isZero();
        assertThat(result.attendanceDates()).isEmpty();
        verify(findUserAttendancePort).findByUserIdAndYearAndMonth(userId, year, month);
    }

    private UserAttendanceHistory createHistory(LocalDateTime attendedAt) {
        return UserAttendanceHistory.from(
                com.personal.marketnote.reward.domain.attendance.UserAttendanceHistorySnapshotState.builder()
                        .id(1L)
                        .userAttendanceId(1L)
                        .attendancePolicyId((short) 1)
                        .continuousPeriod((short) 1)
                        .rewardQuantity(100L)
                        .attendedAt(attendedAt)
                        .build()
        );
    }
}
