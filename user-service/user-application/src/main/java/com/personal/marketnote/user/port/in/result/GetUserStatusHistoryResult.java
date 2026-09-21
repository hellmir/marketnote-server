package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.user.domain.user.UserStatusAction;
import com.personal.marketnote.user.domain.user.UserStatusHistory;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record GetUserStatusHistoryResult(
        Long id,
        Long userId,
        String statusAction,
        String reason,
        LocalDateTime deactivatedUntil,
        Long createdBy,
        LocalDateTime createdAt
) {
    public static GetUserStatusHistoryResult from(UserStatusHistory history) {
        return GetUserStatusHistoryResult.builder()
                .id(history.getId())
                .userId(history.getUserId())
                .statusAction(history.getStatusAction().name())
                .reason(history.getReason())
                .deactivatedUntil(history.getDeactivatedUntil())
                .createdBy(history.getCreatedBy())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
