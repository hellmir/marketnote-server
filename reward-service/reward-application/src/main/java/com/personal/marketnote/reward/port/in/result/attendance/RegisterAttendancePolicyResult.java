package com.personal.marketnote.reward.port.in.result.attendance;

import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterAttendancePolicyResult {
    private final Short id;

    public static RegisterAttendancePolicyResult from(AttendancePolicy policy) {
        return RegisterAttendancePolicyResult.builder()
                .id(policy.getId())
                .build();
    }
}

