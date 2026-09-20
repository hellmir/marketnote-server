package com.personal.marketnote.reward.adapter.in.web.point.response;

import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.port.in.result.point.UserPointHistoryResult;

import java.time.LocalDateTime;

public record UserPointHistoryResponse(
        Long id,
        UserPointChangeType changeType,
        Long amount,
        Boolean isReflected,
        UserPointSourceType sourceType,
        Long sourceId,
        String reason,
        LocalDateTime accumulatedAt,
        LocalDateTime createdAt
) {
    public static UserPointHistoryResponse from(UserPointHistoryResult result) {
        return new UserPointHistoryResponse(
                result.id(),
                result.changeType(),
                result.amount(),
                result.isReflected(),
                result.sourceType(),
                result.sourceId(),
                result.reason(),
                result.accumulatedAt(),
                result.createdAt()
        );
    }
}
