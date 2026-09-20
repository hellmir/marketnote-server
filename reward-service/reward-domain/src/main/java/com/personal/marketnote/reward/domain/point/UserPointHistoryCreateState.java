package com.personal.marketnote.reward.domain.point;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserPointHistoryCreateState {
    private final Long userId;
    private final UserPointChangeType changeType;
    private final Long amount;
    private final Boolean isReflected;
    private final UserPointSourceType sourceType;
    private final Long sourceId;
    private final String reason;
    private final LocalDateTime accumulatedAt;
}
