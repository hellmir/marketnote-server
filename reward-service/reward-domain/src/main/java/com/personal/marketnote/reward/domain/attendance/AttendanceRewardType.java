package com.personal.marketnote.reward.domain.attendance;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AttendanceRewardType {
    POINT("포인트"),
    BOOSTER("영양제 제조 속도 향상 부스터");

    private final String description;
}

