package com.personal.marketnote.reward.service.attendance;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;
import com.personal.marketnote.reward.domain.attendance.UserAttendance;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistory;
import com.personal.marketnote.reward.exception.AttendancePolicyNotFoundException;
import com.personal.marketnote.reward.port.in.result.attendance.GetAttendanceRelayStatusResult;
import com.personal.marketnote.reward.port.in.usecase.attendance.GetAttendanceRelayStatusUseCase;
import com.personal.marketnote.reward.port.out.attendance.FindAttendancePolicyPort;
import com.personal.marketnote.reward.port.out.attendance.FindUserAttendanceHistoryPort;
import com.personal.marketnote.reward.port.out.attendance.FindUserAttendancePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetAttendanceRelayStatusService implements GetAttendanceRelayStatusUseCase {
    private static final short RELAY_CYCLE_SIZE = 4;
    private static final short DEFAULT_POLICY_ID = 10_000;

    private final FindUserAttendancePort findUserAttendancePort;
    private final FindUserAttendanceHistoryPort findUserAttendanceHistoryPort;
    private final FindAttendancePolicyPort findAttendancePolicyPort;

    @Override
    public GetAttendanceRelayStatusResult getRelayStatus(Long userId) {
        LocalDate today = LocalDate.now();
        Year year = Year.from(today.getYear());
        Month month = Month.from(today.getMonthValue());

        Optional<UserAttendance> attendanceOpt =
                findUserAttendancePort.findByUserIdAndYearAndMonth(userId, year, month);

        if (attendanceOpt.isEmpty()) {
            return buildDefaultResult();
        }

        UserAttendance attendance = attendanceOpt.get();
        boolean todayChecked = checkTodayAttendance(attendance.getId(), today);
        Optional<UserAttendanceHistory> latestHistory =
                findUserAttendanceHistoryPort.findLatestByUserAttendanceId(attendance.getId());
        short currentRelayDay = resolveCurrentRelayDay(todayChecked, latestHistory, today);
        List<GetAttendanceRelayStatusResult.RelaySlot> relaySlots =
                buildRelaySlots(currentRelayDay, todayChecked);

        return new GetAttendanceRelayStatusResult(currentRelayDay, todayChecked, relaySlots);
    }

    private GetAttendanceRelayStatusResult buildDefaultResult() {
        List<GetAttendanceRelayStatusResult.RelaySlot> relaySlots = buildRelaySlots((short) 1, false);
        return new GetAttendanceRelayStatusResult((short) 1, false, relaySlots);
    }

    private boolean checkTodayAttendance(Long userAttendanceId, LocalDate today) {
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDayExclusive = startOfDay.plusDays(1);
        return findUserAttendanceHistoryPort.existsByUserAttendanceIdAndAttendedAtBetween(
                userAttendanceId, startOfDay, endOfDayExclusive
        );
    }

    private short resolveCurrentRelayDay(
            boolean todayChecked,
            Optional<UserAttendanceHistory> latestHistory,
            LocalDate today
    ) {
        if (todayChecked && latestHistory.isPresent()) {
            return latestHistory.get().getContinuousPeriod();
        }
        if (isYesterdayAttendance(latestHistory, today)) {
            return (short) ((latestHistory.get().getContinuousPeriod() % RELAY_CYCLE_SIZE) + 1);
        }
        return 1;
    }

    private boolean isYesterdayAttendance(Optional<UserAttendanceHistory> latestHistory, LocalDate today) {
        return latestHistory.isPresent()
                && latestHistory.get().getAttendedAt().toLocalDate().equals(today.minusDays(1));
    }

    private List<GetAttendanceRelayStatusResult.RelaySlot> buildRelaySlots(
            short currentRelayDay,
            boolean todayChecked
    ) {
        List<GetAttendanceRelayStatusResult.RelaySlot> slots = new ArrayList<>();
        for (short day = 1; day <= RELAY_CYCLE_SIZE; day++) {
            AttendancePolicy policy = findPolicyForPeriod(day);
            boolean completed = resolveSlotCompleted(day, currentRelayDay, todayChecked);
            slots.add(new GetAttendanceRelayStatusResult.RelaySlot(
                    day, policy.getRewardType(), policy.getRewardQuantityValue(), completed
            ));
        }
        return slots;
    }

    private AttendancePolicy findPolicyForPeriod(short continuousPeriod) {
        return findAttendancePolicyPort.findByContinuousPeriodAndAttendenceDateIsNull(continuousPeriod)
                .or(() -> findAttendancePolicyPort.findById(DEFAULT_POLICY_ID))
                .orElseThrow(() -> new AttendancePolicyNotFoundException(DEFAULT_POLICY_ID));
    }

    private boolean resolveSlotCompleted(short slotDay, short currentRelayDay, boolean todayChecked) {
        if (todayChecked) {
            return slotDay <= currentRelayDay;
        }
        return slotDay < currentRelayDay;
    }
}
