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
public class UserStatusHistory {
    private Long id;
    private Long userId;
    private UserStatusAction statusAction;
    private String reason;
    private LocalDateTime deactivatedUntil;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static UserStatusHistory of(Long userId, UserStatusAction statusAction, String reason,
                                       LocalDateTime deactivatedUntil, Long createdBy) {
        return UserStatusHistory.builder()
                .userId(userId)
                .statusAction(statusAction)
                .reason(reason)
                .deactivatedUntil(deactivatedUntil)
                .createdBy(createdBy)
                .build();
    }

    public static UserStatusHistory from(UserStatusHistorySnapshotState state) {
        return UserStatusHistory.builder()
                .id(state.getId())
                .userId(state.getUserId())
                .statusAction(state.getStatusAction())
                .reason(state.getReason())
                .deactivatedUntil(state.getDeactivatedUntil())
                .createdBy(state.getCreatedBy())
                .createdAt(state.getCreatedAt())
                .build();
    }
}
