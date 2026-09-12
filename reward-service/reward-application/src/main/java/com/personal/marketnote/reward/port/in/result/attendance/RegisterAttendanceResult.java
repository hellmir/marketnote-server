package com.personal.marketnote.reward.port.in.result.attendance;

import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;

public record RegisterAttendanceResult(
        Long id,
        AttendanceRewardType rewardType,
        long rewardQuantity,
        short continuousPeriod
) {
}
