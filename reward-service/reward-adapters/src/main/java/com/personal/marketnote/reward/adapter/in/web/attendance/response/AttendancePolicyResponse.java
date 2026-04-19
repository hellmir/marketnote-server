package com.personal.marketnote.reward.adapter.in.web.attendance.response;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;
import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AttendancePolicyResponse {
    private final Short id;
    private final short continuousPeriod;
    private final AttendanceRewardType rewardType;
    private final long rewardQuantity;
    private final LocalDate attendenceDate;
    private final EntityStatus status;

    public static AttendancePolicyResponse from(AttendancePolicy policy) {
        return AttendancePolicyResponse.builder()
                .id(policy.getId())
                .continuousPeriod(policy.getContinuousPeriod())
                .rewardType(policy.getRewardType())
                .rewardQuantity(policy.getRewardQuantity())
                .attendenceDate(policy.getAttendenceDate())
                .status(policy.getStatus())
                .build();
    }
}

