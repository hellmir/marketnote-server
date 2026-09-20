package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.user.domain.user.UserPenaltyHistory;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record GetUserPenaltyHistoryResult(
        Long id,
        Long userId,
        int previousCount,
        int currentCount,
        String reason,
        Long createdBy,
        LocalDateTime createdAt
) {
    public static GetUserPenaltyHistoryResult from(UserPenaltyHistory history) {
        return GetUserPenaltyHistoryResult.builder()
                .id(history.getId())
                .userId(history.getUserId())
                .previousCount(history.getPreviousCount())
                .currentCount(history.getCurrentCount())
                .reason(history.getReason())
                .createdBy(history.getCreatedBy())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
