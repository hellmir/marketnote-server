package com.personal.marketnote.reward.domain.point;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserPointSnapshotState {
    private final Long userId;
    private final String userKey;
    private final PointAmount amount;
    private final PointAmount addExpectedAmount;
    private final PointAmount expireExpectedAmount;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
