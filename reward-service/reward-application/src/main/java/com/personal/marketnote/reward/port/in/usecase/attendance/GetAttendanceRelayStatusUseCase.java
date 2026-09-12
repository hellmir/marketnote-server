package com.personal.marketnote.reward.port.in.usecase.attendance;

import com.personal.marketnote.reward.port.in.result.attendance.GetAttendanceRelayStatusResult;

public interface GetAttendanceRelayStatusUseCase {
    GetAttendanceRelayStatusResult getRelayStatus(Long userId);
}
