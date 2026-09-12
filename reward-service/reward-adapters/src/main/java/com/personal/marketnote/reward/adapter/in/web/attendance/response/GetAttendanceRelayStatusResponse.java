package com.personal.marketnote.reward.adapter.in.web.attendance.response;

import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import com.personal.marketnote.reward.port.in.result.attendance.GetAttendanceRelayStatusResult;

import java.util.List;

public record GetAttendanceRelayStatusResponse(
        short currentRelayDay,
        boolean todayChecked,
        List<RelaySlotResponse> relaySlots
) {
    public record RelaySlotResponse(
            short day,
            AttendanceRewardType rewardType,
            long rewardQuantity,
            boolean completed
    ) {
    }

    public static GetAttendanceRelayStatusResponse from(GetAttendanceRelayStatusResult result) {
        List<RelaySlotResponse> slots = result.relaySlots().stream()
                .map(slot -> new RelaySlotResponse(
                        slot.day(), slot.rewardType(), slot.rewardQuantity(), slot.completed()
                ))
                .toList();
        return new GetAttendanceRelayStatusResponse(
                result.currentRelayDay(), result.todayChecked(), slots
        );
    }
}
