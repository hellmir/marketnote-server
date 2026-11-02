package com.personal.marketnote.reward.port.in.command.attendance;

import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record RegisterAttendancePolicyCommand(
        short continuousPeriod,
        AttendanceRewardType rewardType,
        long rewardQuantity,
        LocalDate attendenceDate
) {
}

