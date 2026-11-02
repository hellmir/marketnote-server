package com.personal.marketnote.reward.domain.point;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserPointCreateState {
    private final Long userId;
    private final String userKey;
    private final PointAmount amount;
    private final PointAmount addExpectedAmount;
    private final PointAmount expireExpectedAmount;
}
