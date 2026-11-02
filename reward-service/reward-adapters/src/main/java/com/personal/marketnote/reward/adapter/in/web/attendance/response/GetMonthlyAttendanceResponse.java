package com.personal.marketnote.reward.adapter.in.web.attendance.response;

import com.personal.marketnote.reward.port.in.result.attendance.GetMonthlyAttendanceResult;

import java.time.LocalDate;
import java.util.List;

public record GetMonthlyAttendanceResponse(
        List<LocalDate> attendanceDates, int totalAttendanceDays, long totalRewardQuantity
) {
    public static GetMonthlyAttendanceResponse from(GetMonthlyAttendanceResult result) {
        return new GetMonthlyAttendanceResponse(
                result.attendanceDates(),
                result.totalAttendanceDays(),
                result.totalRewardQuantity()
        );
    }
}
