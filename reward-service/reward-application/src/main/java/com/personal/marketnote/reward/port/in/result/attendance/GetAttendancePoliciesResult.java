package com.personal.marketnote.reward.port.in.result.attendance;

import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;

import java.util.List;

public record GetAttendancePoliciesResult(List<AttendancePolicy> policies) {
    public static GetAttendancePoliciesResult from(List<AttendancePolicy> policies) {
        return new GetAttendancePoliciesResult(policies);
    }
}

