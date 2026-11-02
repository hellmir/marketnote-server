package com.personal.marketnote.reward.port.in.command.attendance;

import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import com.personal.marketnote.common.utility.FormatValidator;

import java.time.LocalDate;

public record GetMonthlyAttendanceQuery(Long userId, Integer year, Integer month) {
    public static GetMonthlyAttendanceQuery of(Long userId, Integer year, Integer month) {
        return new GetMonthlyAttendanceQuery(userId, year, month);
    }

    public Year resolveYear(LocalDate now) {
        int targetYear = FormatValidator.hasValue(year)
                ? year
                : now.getYear();

        return Year.from(targetYear);
    }

    public Month resolveMonth(LocalDate now) {
        int targetMonth = FormatValidator.hasValue(month)
                ? month
                : now.getMonthValue();

        return Month.from(targetMonth);
    }
}
