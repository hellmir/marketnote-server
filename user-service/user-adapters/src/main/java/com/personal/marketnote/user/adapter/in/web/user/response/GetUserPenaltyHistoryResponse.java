package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.GetUserPenaltyHistoryResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record GetUserPenaltyHistoryResponse(
        Long id,
        Long userId,
        int previousCount,
        int currentCount,
        String reason,
        Long createdBy,
        LocalDateTime createdAt
) {
    public static GetUserPenaltyHistoryResponse from(GetUserPenaltyHistoryResult result) {
        return GetUserPenaltyHistoryResponse.builder()
                .id(result.id())
                .userId(result.userId())
                .previousCount(result.previousCount())
                .currentCount(result.currentCount())
                .reason(result.reason())
                .createdBy(result.createdBy())
                .createdAt(result.createdAt())
                .build();
    }
}
