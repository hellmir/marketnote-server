package com.personal.marketnote.user.domain.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class UserPenaltyHistory {
    private Long id;
    private Long userId;
    private int previousCount;
    private int currentCount;
    private String reason;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static UserPenaltyHistory of(Long userId, int previousCount, int currentCount, String reason, Long createdBy) {
        return UserPenaltyHistory.builder()
                .userId(userId)
                .previousCount(previousCount)
                .currentCount(currentCount)
                .reason(reason)
                .createdBy(createdBy)
                .build();
    }

    public static UserPenaltyHistory from(UserPenaltyHistorySnapshotState state) {
        return UserPenaltyHistory.builder()
                .id(state.getId())
                .userId(state.getUserId())
                .previousCount(state.getPreviousCount())
                .currentCount(state.getCurrentCount())
                .reason(state.getReason())
                .createdBy(state.getCreatedBy())
                .createdAt(state.getCreatedAt())
                .build();
    }
}
