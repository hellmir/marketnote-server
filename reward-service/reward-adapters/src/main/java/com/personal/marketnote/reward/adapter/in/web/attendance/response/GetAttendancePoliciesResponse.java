package com.personal.marketnote.reward.adapter.in.web.attendance.response;

import com.personal.marketnote.reward.port.in.result.attendance.GetAttendancePoliciesResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GetAttendancePoliciesResponse {
    private final List<AttendancePolicyResponse> policies;

    public static GetAttendancePoliciesResponse from(GetAttendancePoliciesResult result) {
        return GetAttendancePoliciesResponse.builder()
                .policies(result.policies().stream()
                        .map(AttendancePolicyResponse::from)
                        .toList())
                .build();
    }
}

