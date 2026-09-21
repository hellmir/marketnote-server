package com.personal.marketnote.reward.service.attendance;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import com.personal.marketnote.reward.domain.attendance.ContinuousPeriod;
import com.personal.marketnote.reward.domain.attendance.*;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.exception.InvalidAttendanceTimeException;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.port.in.command.attendance.RegisterAttendanceCommand;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.port.in.result.attendance.RegisterAttendanceResult;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.port.out.attendance.*;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
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
                            .totalRewardQuantity(RewardQuantity.of(100L))
                            .histories(List.of())
                            .build()
            );

            AttendancePolicy policy = AttendancePolicy.from(
                    AttendancePolicySnapshotState.builder()
                            .id((short) 1)
                            .continuousPeriod(ContinuousPeriod.of((short) 1))
                            .rewardType(AttendanceRewardType.POINT)
                            .rewardQuantity(RewardQuantity.of(50L))
                            .status(EntityStatus.ACTIVE)
                            .build()
            );

            UserAttendanceHistory savedHistory = UserAttendanceHistory.from(
                    UserAttendanceHistorySnapshotState.builder()
                            .id(100L)
                            .userAttendanceId(10L)
                            .attendancePolicyId((short) 1)
                            .continuousPeriod(ContinuousPeriod.of((short) 1))
                            .rewardQuantity(RewardQuantity.of(50L))
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
            assertThat(result.id()).isEqualTo(100L);
            assertThat(result.rewardType()).isEqualTo(AttendanceRewardType.POINT);
            assertThat(result.rewardQuantity()).isEqualTo(50L);
            assertThat(result.continuousPeriod()).isEqualTo((short) 1);
            verify(saveUserAttendanceHistoryPort).save(any(UserAttendanceHistory.class));
            verify(saveUserAttendancePort).save(any(UserAttendance.class));
        }
    }

    @Nested
    @DisplayName("4일 주기 릴레이 순환")
    class RelayCycleTest {

        @Test
        @DisplayName("연속 2일차 출석 시 continuousPeriod가 2로 증가한다")
        void shouldIncreaseContinuousPeriodToTwoOnSecondConsecutiveDay() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime yesterday = now.minusDays(1);
            RegisterAttendanceCommand command = RegisterAttendanceCommand.builder()
                    .userId(1L)
                    .attendedAt(now)
                    .build();

            UserAttendance existingAttendance = createUserAttendance(now);

            UserAttendanceHistory lastHistory = UserAttendanceHistory.from(
                    UserAttendanceHistorySnapshotState.builder()
                            .id(99L)
                            .userAttendanceId(10L)
                            .attendancePolicyId((short) 1)
                            .continuousPeriod(ContinuousPeriod.of((short) 1))
                            .rewardQuantity(RewardQuantity.of(50L))
                            .attendedAt(yesterday)
                            .build()
            );

            AttendancePolicy policy = createPolicy((short) 2, AttendanceRewardType.POINT, 100L);

            UserAttendanceHistory savedHistory = UserAttendanceHistory.from(
                    UserAttendanceHistorySnapshotState.builder()
                            .id(100L)
                            .userAttendanceId(10L)
                            .attendancePolicyId((short) 2)
                            .continuousPeriod(ContinuousPeriod.of((short) 2))
                            .rewardQuantity(RewardQuantity.of(100L))
                            .attendedAt(now)
                            .build()
            );

            stubCommonMocks(command, existingAttendance, now);
            when(findUserAttendanceHistoryPort.findLatestByUserAttendanceId(10L))
                    .thenReturn(Optional.of(lastHistory));
            when(findAttendancePolicyPort.findByContinuousPeriodAndAttendenceDate(
                    (short) 2, now.toLocalDate()
            )).thenReturn(Optional.of(policy));
            when(saveUserAttendanceHistoryPort.save(any(UserAttendanceHistory.class)))
                    .thenReturn(savedHistory);

            // when
            RegisterAttendanceResult result = registerAttendanceService.register(command);

            // then
            assertThat(result.continuousPeriod()).isEqualTo((short) 2);
            assertThat(result.rewardQuantity()).isEqualTo(100L);
        }

        @Test
        @DisplayName("연속 4일차까지 출석 시 continuousPeriod가 4까지 증가한다")
        void shouldIncreaseContinuousPeriodToFourOnFourthConsecutiveDay() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime yesterday = now.minusDays(1);
            RegisterAttendanceCommand command = RegisterAttendanceCommand.builder()
                    .userId(1L)
                    .attendedAt(now)
                    .build();

            UserAttendance existingAttendance = createUserAttendance(now);

            UserAttendanceHistory lastHistory = UserAttendanceHistory.from(
                    UserAttendanceHistorySnapshotState.builder()
                            .id(99L)
                            .userAttendanceId(10L)
                            .attendancePolicyId((short) 3)
                            .continuousPeriod(ContinuousPeriod.of((short) 3))
                            .rewardQuantity(RewardQuantity.of(150L))
                            .attendedAt(yesterday)
                            .build()
            );

            AttendancePolicy policy = createPolicy((short) 4, AttendanceRewardType.BOOSTER, 1L);

            UserAttendanceHistory savedHistory = UserAttendanceHistory.from(
                    UserAttendanceHistorySnapshotState.builder()
                            .id(100L)
                            .userAttendanceId(10L)
                            .attendancePolicyId((short) 4)
                            .continuousPeriod(ContinuousPeriod.of((short) 4))
                            .rewardQuantity(RewardQuantity.of(1L))
                            .attendedAt(now)
                            .build()
            );

            stubCommonMocks(command, existingAttendance, now);
            when(findUserAttendanceHistoryPort.findLatestByUserAttendanceId(10L))
                    .thenReturn(Optional.of(lastHistory));
            when(findAttendancePolicyPort.findByContinuousPeriodAndAttendenceDate(
                    (short) 4, now.toLocalDate()
            )).thenReturn(Optional.of(policy));
            when(saveUserAttendanceHistoryPort.save(any(UserAttendanceHistory.class)))
                    .thenReturn(savedHistory);

            // when
            RegisterAttendanceResult result = registerAttendanceService.register(command);

            // then
            assertThat(result.continuousPeriod()).isEqualTo((short) 4);
            assertThat(result.rewardType()).isEqualTo(AttendanceRewardType.BOOSTER);
        }

        @Test
        @DisplayName("4일차 완료 후 연속 출석하면 continuousPeriod가 1로 리셋된다")
        void shouldResetContinuousPeriodToOneAfterCompletingFourDayCycle() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime yesterday = now.minusDays(1);
            RegisterAttendanceCommand command = RegisterAttendanceCommand.builder()
                    .userId(1L)
                    .attendedAt(now)
                    .build();

            UserAttendance existingAttendance = createUserAttendance(now);

            UserAttendanceHistory lastHistory = UserAttendanceHistory.from(
                    UserAttendanceHistorySnapshotState.builder()
                            .id(99L)
                            .userAttendanceId(10L)
                            .attendancePolicyId((short) 4)
                            .continuousPeriod(ContinuousPeriod.of((short) 4))
                            .rewardQuantity(RewardQuantity.of(1L))
                            .attendedAt(yesterday)
                            .build()
            );

            AttendancePolicy policy = createPolicy((short) 1, AttendanceRewardType.POINT, 50L);

            UserAttendanceHistory savedHistory = UserAttendanceHistory.from(
                    UserAttendanceHistorySnapshotState.builder()
                            .id(100L)
                            .userAttendanceId(10L)
                            .attendancePolicyId((short) 1)
                            .continuousPeriod(ContinuousPeriod.of((short) 1))
                            .rewardQuantity(RewardQuantity.of(50L))
                            .attendedAt(now)
                            .build()
            );

            stubCommonMocks(command, existingAttendance, now);
            when(findUserAttendanceHistoryPort.findLatestByUserAttendanceId(10L))
                    .thenReturn(Optional.of(lastHistory));
            when(findAttendancePolicyPort.findByContinuousPeriodAndAttendenceDate(
                    (short) 1, now.toLocalDate()
            )).thenReturn(Optional.of(policy));
            when(saveUserAttendanceHistoryPort.save(any(UserAttendanceHistory.class)))
                    .thenReturn(savedHistory);

            // when
            RegisterAttendanceResult result = registerAttendanceService.register(command);

            // then
            assertThat(result.continuousPeriod()).isEqualTo((short) 1);
        }

        @Test
        @DisplayName("출석 스킵 후 출석하면 continuousPeriod가 1로 리셋된다")
        void shouldResetContinuousPeriodToOneWhenAttendanceSkipped() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twoDaysAgo = now.minusDays(2);
            RegisterAttendanceCommand command = RegisterAttendanceCommand.builder()
                    .userId(1L)
                    .attendedAt(now)
                    .build();

            UserAttendance existingAttendance = createUserAttendance(now);

            UserAttendanceHistory lastHistory = UserAttendanceHistory.from(
                    UserAttendanceHistorySnapshotState.builder()
                            .id(99L)
                            .userAttendanceId(10L)
                            .attendancePolicyId((short) 2)
                            .continuousPeriod(ContinuousPeriod.of((short) 2))
                            .rewardQuantity(RewardQuantity.of(100L))
                            .attendedAt(twoDaysAgo)
                            .build()
            );

            AttendancePolicy policy = createPolicy((short) 1, AttendanceRewardType.POINT, 50L);

            UserAttendanceHistory savedHistory = UserAttendanceHistory.from(
                    UserAttendanceHistorySnapshotState.builder()
                            .id(100L)
                            .userAttendanceId(10L)
                            .attendancePolicyId((short) 1)
                            .continuousPeriod(ContinuousPeriod.of((short) 1))
                            .rewardQuantity(RewardQuantity.of(50L))
                            .attendedAt(now)
                            .build()
            );

            stubCommonMocks(command, existingAttendance, now);
            when(findUserAttendanceHistoryPort.findLatestByUserAttendanceId(10L))
                    .thenReturn(Optional.of(lastHistory));
            when(findAttendancePolicyPort.findByContinuousPeriodAndAttendenceDate(
                    (short) 1, now.toLocalDate()
            )).thenReturn(Optional.of(policy));
            when(saveUserAttendanceHistoryPort.save(any(UserAttendanceHistory.class)))
                    .thenReturn(savedHistory);

            // when
            RegisterAttendanceResult result = registerAttendanceService.register(command);

            // then
            assertThat(result.continuousPeriod()).isEqualTo((short) 1);
        }

        @Test
        @DisplayName("출석 등록 응답에 보상 정보가 포함된다")
        void shouldIncludeRewardInfoInResult() {
            // given
            LocalDateTime now = LocalDateTime.now();
            RegisterAttendanceCommand command = RegisterAttendanceCommand.builder()
                    .userId(1L)
                    .attendedAt(now)
                    .build();

            UserAttendance existingAttendance = createUserAttendance(now);
            AttendancePolicy policy = createPolicy((short) 1, AttendanceRewardType.BOOSTER, 3L);

            UserAttendanceHistory savedHistory = UserAttendanceHistory.from(
                    UserAttendanceHistorySnapshotState.builder()
                            .id(100L)
                            .userAttendanceId(10L)
                            .attendancePolicyId((short) 1)
                            .continuousPeriod(ContinuousPeriod.of((short) 1))
                            .rewardQuantity(RewardQuantity.of(3L))
                            .attendedAt(now)
                            .build()
            );

            stubCommonMocks(command, existingAttendance, now);
            when(findUserAttendanceHistoryPort.findLatestByUserAttendanceId(10L))
                    .thenReturn(Optional.empty());
            when(findAttendancePolicyPort.findByContinuousPeriodAndAttendenceDate(
                    (short) 1, now.toLocalDate()
            )).thenReturn(Optional.of(policy));
            when(saveUserAttendanceHistoryPort.save(any(UserAttendanceHistory.class)))
                    .thenReturn(savedHistory);

            // when
            RegisterAttendanceResult result = registerAttendanceService.register(command);

            // then
            assertThat(result.rewardType()).isEqualTo(AttendanceRewardType.BOOSTER);
            assertThat(result.rewardQuantity()).isEqualTo(3L);
            assertThat(result.continuousPeriod()).isEqualTo((short) 1);
        }

        private UserAttendance createUserAttendance(LocalDateTime dateTime) {
            return UserAttendance.from(
                    UserAttendanceSnapshotState.builder()
                            .id(10L)
                            .userId(1L)
                            .year(Year.from(dateTime.getYear()))
                            .month(Month.from(dateTime.getMonthValue()))
                            .totalRewardQuantity(RewardQuantity.of(100L))
                            .histories(List.of())
                            .build()
            );
        }

        private AttendancePolicy createPolicy(short continuousPeriod, AttendanceRewardType rewardType, long rewardQuantity) {
            return AttendancePolicy.from(
                    AttendancePolicySnapshotState.builder()
                            .id(continuousPeriod)
                            .continuousPeriod(ContinuousPeriod.of(continuousPeriod))
                            .rewardType(rewardType)
                            .rewardQuantity(RewardQuantity.of(rewardQuantity))
                            .status(EntityStatus.ACTIVE)
                            .build()
            );
        }

        private void stubCommonMocks(RegisterAttendanceCommand command, UserAttendance attendance, LocalDateTime now) {
            when(findUserAttendancePort.findByUserIdAndYearAndMonth(
                    command.userId(), Year.from(now.getYear()), Month.from(now.getMonthValue())
            )).thenReturn(Optional.of(attendance));
            when(findUserAttendanceHistoryPort.existsByUserAttendanceIdAndAttendedAtBetween(
                    eq(10L), any(), any()
            )).thenReturn(false);
            when(saveUserAttendancePort.save(any(UserAttendance.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
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
                            .totalRewardQuantity(RewardQuantity.of(0L))
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
