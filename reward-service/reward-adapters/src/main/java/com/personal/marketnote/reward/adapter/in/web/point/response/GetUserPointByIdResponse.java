package com.personal.marketnote.reward.adapter.in.web.point.response;

import com.personal.marketnote.reward.port.in.result.point.GetUserPointResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record GetUserPointByIdResponse(
        Long userId,
        Long amount,
        Long addExpectedAmount,
        Long expireExpectedAmount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static GetUserPointByIdResponse from(GetUserPointResult result) {
        return GetUserPointByIdResponse.builder()
                .userId(result.userId())
                .amount(result.amount())
                .addExpectedAmount(result.addExpectedAmount())
                .expireExpectedAmount(result.expireExpectedAmount())
                .createdAt(result.createdAt())
                .modifiedAt(result.modifiedAt())
                .build();
    }
}
