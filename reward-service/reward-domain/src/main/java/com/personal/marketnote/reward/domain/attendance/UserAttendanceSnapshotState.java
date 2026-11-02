package com.personal.marketnote.reward.domain.attendance;

import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserAttendanceSnapshotState {
    private final Long id;
    private final Long userId;
    private final Year year;
    private final Month month;
    private final LocalDateTime createdAt;
    private final RewardQuantity totalRewardQuantity;
    private final java.util.List<UserAttendanceHistory> histories;
}

