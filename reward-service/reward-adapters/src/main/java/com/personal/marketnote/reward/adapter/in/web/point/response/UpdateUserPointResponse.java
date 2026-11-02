package com.personal.marketnote.reward.adapter.in.web.point.response;

import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;

import java.time.LocalDateTime;

public record UpdateUserPointResponse(
        Long userId,
        Long amount,
        Long addExpectedAmount,
        Long expireExpectedAmount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static UpdateUserPointResponse from(UpdateUserPointResult result) {
        return new UpdateUserPointResponse(
                result.userId(),
                result.amount(),
                result.addExpectedAmount(),
                result.expireExpectedAmount(),
                result.createdAt(),
                result.modifiedAt()
        );
    }
}
