package com.personal.marketnote.user.adapter.in.web.user.response;

import com.personal.marketnote.user.port.in.result.GetUserStatusHistoryResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record GetUserStatusHistoryResponse(
        Long id,
        Long userId,
        String statusAction,
        String reason,
        LocalDateTime deactivatedUntil,
        Long createdBy,
        LocalDateTime createdAt
) {
    public static GetUserStatusHistoryResponse from(GetUserStatusHistoryResult result) {
        return GetUserStatusHistoryResponse.builder()
                .id(result.id())
                .userId(result.userId())
                .statusAction(result.statusAction())
                .reason(result.reason())
                .deactivatedUntil(result.deactivatedUntil())
                .createdBy(result.createdBy())
                .createdAt(result.createdAt())
                .build();
    }
}
