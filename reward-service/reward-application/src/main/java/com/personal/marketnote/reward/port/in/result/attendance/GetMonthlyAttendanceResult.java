package com.personal.marketnote.reward.port.in.result.attendance;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.attendance.UserAttendance;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record GetMonthlyAttendanceResult(
        List<LocalDate> attendanceDates,
        int totalAttendanceDays,
        long totalRewardQuantity
) {
    public static GetMonthlyAttendanceResult from(UserAttendance attendance) {
        if (FormatValidator.hasNoValue(attendance)) {
            return empty();
        }

        List<UserAttendanceHistory> histories = attendance.getHistories();
        List<LocalDate> dates = FormatValidator.hasNoValue(histories)
                ? List.of()
                : histories.stream()
                .map(UserAttendanceHistory::getAttendedAt)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .sorted()
                .toList();

        return new GetMonthlyAttendanceResult(
                dates,
                dates.size(),
                attendance.getTotalRewardQuantity()
        );
    }

    public static GetMonthlyAttendanceResult empty() {
        return new GetMonthlyAttendanceResult(List.of(), 0, 0L);
    }
}
