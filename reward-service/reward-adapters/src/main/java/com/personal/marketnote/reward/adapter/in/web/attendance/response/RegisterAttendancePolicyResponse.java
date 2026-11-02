package com.personal.marketnote.reward.adapter.in.web.attendance.response;

import com.personal.marketnote.reward.port.in.result.attendance.RegisterAttendancePolicyResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterAttendancePolicyResponse {
    private final Short id;

    public static RegisterAttendancePolicyResponse from(RegisterAttendancePolicyResult result) {
        return RegisterAttendancePolicyResponse.builder()
                .id(result.getId())
                .build();
    }
}

