package com.personal.marketnote.reward.port.in.result.point;

import com.personal.marketnote.reward.domain.point.UserPoint;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record GetUserPointResult(
        Long userId,
        Long amount,
        Long addExpectedAmount,
        Long expireExpectedAmount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static GetUserPointResult from(UserPoint userPoint) {
        return GetUserPointResult.builder()
                .userId(userPoint.getUserId())
                .amount(userPoint.getAmountValue())
                .addExpectedAmount(userPoint.getAddExpectedAmount().getValue())
                .expireExpectedAmount(userPoint.getExpireExpectedAmount().getValue())
                .createdAt(userPoint.getCreatedAt())
                .modifiedAt(userPoint.getModifiedAt())
                .build();
    }
}
