package com.personal.marketnote.reward.domain.attendance;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AttendancePolicyCreateState {
    private final ContinuousPeriod continuousPeriod;
    private final AttendanceRewardType rewardType;
    private final RewardQuantity rewardQuantity;
    private final LocalDate attendenceDate;
}

