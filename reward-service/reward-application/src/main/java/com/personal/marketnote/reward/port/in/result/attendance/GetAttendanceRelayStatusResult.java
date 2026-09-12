package com.personal.marketnote.reward.port.in.result.attendance;

import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;

import java.util.List;

public record GetAttendanceRelayStatusResult(
        short currentRelayDay,
        boolean todayChecked,
        List<RelaySlot> relaySlots
) {
    public record RelaySlot(
            short day,
            AttendanceRewardType rewardType,
            long rewardQuantity,
            boolean completed
    ) {
    }
}
