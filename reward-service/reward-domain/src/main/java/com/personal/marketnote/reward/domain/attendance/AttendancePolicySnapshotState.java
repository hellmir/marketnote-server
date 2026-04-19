package com.personal.marketnote.reward.domain.attendance;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class AttendancePolicySnapshotState {
    private final Short id;
    private final short continuousPeriod;
    private final AttendanceRewardType rewardType;
    private final long rewardQuantity;
    private final LocalDate attendenceDate;
    private final EntityStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}

