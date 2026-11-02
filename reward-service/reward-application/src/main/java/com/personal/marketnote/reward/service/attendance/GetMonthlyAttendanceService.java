package com.personal.marketnote.reward.service.attendance;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import com.personal.marketnote.reward.port.in.command.attendance.GetMonthlyAttendanceQuery;
import com.personal.marketnote.reward.port.in.result.attendance.GetMonthlyAttendanceResult;
import com.personal.marketnote.reward.port.in.usecase.attendance.GetMonthlyAttendanceUseCase;
import com.personal.marketnote.reward.port.out.attendance.FindUserAttendancePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetMonthlyAttendanceService implements GetMonthlyAttendanceUseCase {
    private final FindUserAttendancePort findUserAttendancePort;

    @Override
    public GetMonthlyAttendanceResult getMonthlyAttendanceHistories(GetMonthlyAttendanceQuery query) {
        LocalDate now = LocalDate.now();
        Year year = query.resolveYear(now);
        Month month = query.resolveMonth(now);

        return findUserAttendancePort.findByUserIdAndYearAndMonth(query.userId(), year, month)
                .map(GetMonthlyAttendanceResult::from)
                .orElseGet(GetMonthlyAttendanceResult::empty);
    }
}
