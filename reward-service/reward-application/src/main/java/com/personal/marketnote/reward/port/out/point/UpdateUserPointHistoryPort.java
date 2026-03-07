package com.personal.marketnote.reward.port.out.point;

import com.personal.marketnote.reward.domain.point.UserPointSourceType;

public interface UpdateUserPointHistoryPort {
    int markAsReflected(Long userId, UserPointSourceType sourceType, Long sourceId);
}
