package com.personal.marketnote.reward.service.attendance;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;
import com.personal.marketnote.reward.domain.attendance.UserAttendance;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistory;
import com.personal.marketnote.reward.exception.InvalidAttendanceTimeException;
import com.personal.marketnote.reward.mapper.RewardCommandToStateMapper;
import com.personal.marketnote.reward.port.in.command.attendance.RegisterAttendanceCommand;
import com.personal.marketnote.reward.port.in.result.attendance.RegisterAttendanceResult;
import com.personal.marketnote.reward.port.in.usecase.attendance.RegisterAttendanceUseCase;
import com.personal.marketnote.reward.port.out.attendance.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterAttendanceService implements RegisterAttendanceUseCase {
    private static final long ALLOWED_DELAY_MINUTES = 1L;
    private static final short DEFAULT_POLICY_ID = 10_000;

    private final SaveUserAttendanceHistoryPort saveUserAttendanceHistoryPort;
    private final SaveUserAttendancePort saveUserAttendancePort;
    private final FindUserAttendancePort findUserAttendancePort;
    private final FindUserAttendanceHistoryPort findUserAttendanceHistoryPort;
    private final FindAttendancePolicyPort findAttendancePolicyPort;

    @Override
    public RegisterAttendanceResult register(RegisterAttendanceCommand command) {
        validateAttendedAt(command.attendedAt());

        LocalDate attendedDate = command.attendedAt().toLocalDate();
        UserAttendance userAttendance = findOrCreateUserAttendance(command.userId(), attendedDate);

        assertNoDuplicateAttendanceToday(userAttendance.getId(), command.attendedAt());

        short continuousPeriod = calculateContinuousPeriod(userAttendance.getId(), command.attendedAt());
        AttendancePolicy attendancePolicy = selectAttendancePolicy(continuousPeriod, attendedDate);
        UserAttendanceHistory savedHistory = saveUserAttendanceHistoryPort.save(
                UserAttendanceHistory.from(
                        RewardCommandToStateMapper.mapToUserAttendanceHistoryCreateState(
                                command,
                                attendancePolicy,
                                continuousPeriod,
                                userAttendance.getId()
                        )
                )
        );

        saveUserAttendancePort.save(
                userAttendance.withAddedReward(attendancePolicy.getRewardQuantity())
        );

        return RegisterAttendanceResult.builder()
                .id(savedHistory.getId())
                .build();
    }

    private void validateAttendedAt(LocalDateTime attendedAt) {
        LocalDateTime now = LocalDateTime.now();
        if (
                FormatValidator.hasNoValue(attendedAt)
                        || attendedAt.isAfter(now)
                        || attendedAt.isBefore(now.minusMinutes(ALLOWED_DELAY_MINUTES))
        ) {
            throw new InvalidAttendanceTimeException("전송된 출석일시가 올바르지 않습니다.");
        }
    }

    private UserAttendance findOrCreateUserAttendance(Long userId, LocalDate attendedDate) {
        Year year = Year.from(attendedDate.getYear());
        Month month = Month.from(attendedDate.getMonthValue());

        return findUserAttendancePort.findByUserIdAndYearAndMonth(userId, year, month)
                .orElseGet(() -> saveUserAttendancePort.save(
                        UserAttendance.from(
                                RewardCommandToStateMapper.mapToUserAttendanceCreateState(userId, year, month)
                        )
                ));
    }

    private short calculateContinuousPeriod(Long userAttendanceId, LocalDateTime attendedAt) {
        LocalDate attendedDate = attendedAt.toLocalDate();

        return findUserAttendanceHistoryPort.findLatestByUserAttendanceId(userAttendanceId)
                .filter(last -> last.getAttendedAt().toLocalDate().equals(attendedDate.minusDays(1)))
                .map(last -> (short) (last.getContinuousPeriod() + 1))
                .orElse((short) 1);
    }

    private void assertNoDuplicateAttendanceToday(Long userAttendanceId, LocalDateTime attendedAt) {
        LocalDateTime startOfDay = attendedAt.toLocalDate().atStartOfDay();
        LocalDateTime endOfDayExclusive = startOfDay.plusDays(1);
        if (findUserAttendanceHistoryPort.existsByUserAttendanceIdAndAttendedAtBetween(
                userAttendanceId, startOfDay, endOfDayExclusive
        )) {
            throw new InvalidAttendanceTimeException("이미 오늘 출석이 등록되었습니다.");
        }
    }

    private AttendancePolicy selectAttendancePolicy(short continuousPeriod, LocalDate attendedDate) {
        return findAttendancePolicyPort.findByContinuousPeriodAndAttendenceDate(continuousPeriod, attendedDate)
                .or(() -> findAttendancePolicyPort.findByContinuousPeriodAndAttendenceDateIsNull(continuousPeriod))
                .or(() -> findAttendancePolicyPort.findById(DEFAULT_POLICY_ID))
                .orElseThrow(() -> new IllegalStateException("기본 출석 정책을 찾을 수 없습니다. 서버 담당자에게 문의 바랍니다."));
    }
}
