package com.personal.marketnote.reward.port.in.result.point;

import com.personal.marketnote.reward.domain.point.UserPoint;

import java.time.LocalDateTime;

public record UpdateUserPointResult(
        Long userId,
        Long amount,
        Long addExpectedAmount,
        Long expireExpectedAmount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static UpdateUserPointResult from(UserPoint userPoint) {
        return new UpdateUserPointResult(
                userPoint.getUserId(),
                userPoint.getAmountValue(),
                userPoint.getAddExpectedAmountValue(),
                userPoint.getExpireExpectedAmountValue(),
                userPoint.getCreatedAt(),
                userPoint.getModifiedAt()
        );
    }
}
