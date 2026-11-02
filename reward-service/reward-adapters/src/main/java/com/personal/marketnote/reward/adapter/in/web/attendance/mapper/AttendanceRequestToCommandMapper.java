package com.personal.marketnote.reward.adapter.in.web.attendance.mapper;

import com.personal.marketnote.reward.adapter.in.web.attendance.request.RegisterAttendancePolicyRequest;
import com.personal.marketnote.reward.adapter.in.web.attendance.request.RegisterAttendanceRequest;
import com.personal.marketnote.reward.port.in.command.attendance.RegisterAttendanceCommand;
import com.personal.marketnote.reward.port.in.command.attendance.RegisterAttendancePolicyCommand;

public class AttendanceRequestToCommandMapper {
    public static RegisterAttendanceCommand mapToCommand(Long userId, RegisterAttendanceRequest request) {
        return RegisterAttendanceCommand.builder()
                .userId(userId)
                .attendedAt(request.getAttendedAt())
                .build();
    }

    public static RegisterAttendancePolicyCommand mapToCommand(RegisterAttendancePolicyRequest request) {
        return RegisterAttendancePolicyCommand.builder()
                .continuousPeriod(request.getContinuousPeriod())
                .rewardType(request.getRewardType())
                .rewardQuantity(request.getRewardQuantity())
                .attendenceDate(request.getAttendenceDate())
                .build();
    }
}

