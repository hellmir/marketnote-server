package com.personal.marketnote.reward.service.attendance;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import com.personal.marketnote.reward.domain.attendance.*;
import com.personal.marketnote.reward.exception.InvalidAttendanceTimeException;
import com.personal.marketnote.reward.port.in.command.attendance.RegisterAttendanceCommand;
import com.personal.marketnote.reward.port.in.result.attendance.RegisterAttendanceResult;
import com.personal.marketnote.reward.port.out.attendance.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterAttendanceUseCase 테스트")
class RegisterAttendanceUseCaseTest {

    @InjectMocks
    private RegisterAttendanceService registerAttendanceService;

    @Mock
    private SaveUserAttendanceHistoryPort saveUserAttendanceHistoryPort;

    @Mock
    private SaveUserAttendancePort saveUserAttendancePort;

    @Mock
    private FindUserAttendancePort findUserAttendancePort;

    @Mock
    private FindUserAttendanceHistoryPort findUserAttendanceHistoryPort;

    @Mock
    private FindAttendancePolicyPort findAttendancePolicyPort;

    @Nested
    @DisplayName("출석 등록 성공")
    class RegisterSuccessTest {

        @Test
        @DisplayName("기존 월별 출석이 있고 연속 출석 정책이 있으면 출석이 등록된다")
        void shouldRegisterAttendanceWithExistingMonthlyAttendance() {
            // given
            LocalDateTime now = LocalDateTime.now();
            RegisterAttendanceCommand command = RegisterAttendanceCommand.builder()
                    .userId(1L)
                    .attendedAt(now)
                    .build();

            UserAttendance existingAttendance = UserAttendance.from(
                    UserAttendanceSnapshotState.builder()
                            .id(10L)
                            .userId(1L)
                            .year(Year.from(now.getYear()))
                            .month(Month.from(now.getMonthValue()))
                            .totalRewardQuantity(100L)
                            .histories(List.of())
                            .build()
            );

            AttendancePolicy policy = AttendancePolicy.from(
                    AttendancePolicySnapshotState.builder()
                            .id((short) 1)
                            .continuousPeriod((short) 1)
                            .rewardType(AttendanceRewardType.POINT)
                            .rewardQuantity(50L)
                            .status(EntityStatus.ACTIVE)
                            .build()
            );

            UserAttendanceHistory savedHistory = UserAttendanceHistory.from(
                    UserAttendanceHistorySnapshotState.builder()
                            .id(100L)
                            .userAttendanceId(10L)
                            .attendancePolicyId((short) 1)
                            .continuousPeriod((short) 1)
                            .rewardQuantity(50L)
                            .attendedAt(now)
                            .build()
            );

            when(findUserAttendancePort.findByUserIdAndYearAndMonth(
                    1L, Year.from(now.getYear()), Month.from(now.getMonthValue())
            )).thenReturn(Optional.of(existingAttendance));
            when(findUserAttendanceHistoryPort.existsByUserAttendanceIdAndAttendedAtBetween(
                    eq(10L), any(), any()
            )).thenReturn(false);
            when(findUserAttendanceHistoryPort.findLatestByUserAttendanceId(10L))
                    .thenReturn(Optional.empty());
            when(findAttendancePolicyPort.findByContinuousPeriodAndAttendenceDate(
                    (short) 1, now.toLocalDate()
            )).thenReturn(Optional.of(policy));
            when(saveUserAttendanceHistoryPort.save(any(UserAttendanceHistory.class)))
                    .thenReturn(savedHistory);
            when(saveUserAttendancePort.save(any(UserAttendance.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            RegisterAttendanceResult result = registerAttendanceService.register(command);

            // then
            assertThat(result.getId()).isEqualTo(100L);
            verify(saveUserAttendanceHistoryPort).save(any(UserAttendanceHistory.class));
            verify(saveUserAttendancePort).save(any(UserAttendance.class));
        }
    }

    @Nested
    @DisplayName("출석 등록 실패")
    class RegisterFailureTest {

        @Test
        @DisplayName("출석 일시가 null이면 InvalidAttendanceTimeException이 발생한다")
        void shouldThrowWhenAttendedAtIsNull() {
            // given
            RegisterAttendanceCommand command = RegisterAttendanceCommand.builder()
                    .userId(1L)
                    .attendedAt(null)
                    .build();

            // when & then
            assertThatThrownBy(() -> registerAttendanceService.register(command))
                    .isInstanceOf(InvalidAttendanceTimeException.class);

            verifyNoInteractions(findUserAttendancePort, saveUserAttendanceHistoryPort,
                    saveUserAttendancePort, findUserAttendanceHistoryPort, findAttendancePolicyPort);
        }

        @Test
        @DisplayName("출석 일시가 미래이면 InvalidAttendanceTimeException이 발생한다")
        void shouldThrowWhenAttendedAtIsFuture() {
            // given
            RegisterAttendanceCommand command = RegisterAttendanceCommand.builder()
                    .userId(1L)
                    .attendedAt(LocalDateTime.now().plusMinutes(5))
                    .build();

            // when & then
            assertThatThrownBy(() -> registerAttendanceService.register(command))
                    .isInstanceOf(InvalidAttendanceTimeException.class);

            verifyNoInteractions(findUserAttendancePort, saveUserAttendanceHistoryPort);
        }

        @Test
        @DisplayName("이미 오늘 출석했으면 InvalidAttendanceTimeException이 발생한다")
        void shouldThrowWhenAlreadyAttendedToday() {
            // given
            LocalDateTime now = LocalDateTime.now();
            RegisterAttendanceCommand command = RegisterAttendanceCommand.builder()
                    .userId(1L)
                    .attendedAt(now)
                    .build();

            UserAttendance existingAttendance = UserAttendance.from(
                    UserAttendanceSnapshotState.builder()
                            .id(10L)
                            .userId(1L)
                            .year(Year.from(now.getYear()))
                            .month(Month.from(now.getMonthValue()))
                            .totalRewardQuantity(0L)
                            .histories(List.of())
                            .build()
            );

            when(findUserAttendancePort.findByUserIdAndYearAndMonth(
                    1L, Year.from(now.getYear()), Month.from(now.getMonthValue())
            )).thenReturn(Optional.of(existingAttendance));
            when(findUserAttendanceHistoryPort.existsByUserAttendanceIdAndAttendedAtBetween(
                    eq(10L), any(), any()
            )).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> registerAttendanceService.register(command))
                    .isInstanceOf(InvalidAttendanceTimeException.class);

            verifyNoInteractions(saveUserAttendanceHistoryPort, saveUserAttendancePort, findAttendancePolicyPort);
        }
    }
}
