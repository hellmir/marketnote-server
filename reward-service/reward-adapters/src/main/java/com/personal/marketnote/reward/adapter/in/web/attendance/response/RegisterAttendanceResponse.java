package com.personal.marketnote.reward.adapter.in.web.attendance.response;

import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import com.personal.marketnote.reward.port.in.result.attendance.RegisterAttendanceResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterAttendanceResponse {
    private final Long id;
    private final AttendanceRewardType rewardType;
    private final long rewardQuantity;
    private final short continuousPeriod;

    public static RegisterAttendanceResponse from(RegisterAttendanceResult result) {
        return RegisterAttendanceResponse.builder()
                .id(result.id())
                .rewardType(result.rewardType())
                .rewardQuantity(result.rewardQuantity())
                .continuousPeriod(result.continuousPeriod())
                .build();
    }
}

