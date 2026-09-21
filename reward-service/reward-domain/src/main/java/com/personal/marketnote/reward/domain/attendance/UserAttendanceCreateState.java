package com.personal.marketnote.reward.domain.attendance;

import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserAttendanceCreateState {
    private final Long userId;
    private final Year year;
    private final Month month;
    private final RewardQuantity totalRewardQuantity;
    private final java.util.List<UserAttendanceHistory> histories;
}

