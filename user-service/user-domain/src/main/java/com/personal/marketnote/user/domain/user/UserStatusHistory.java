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
    private UserStatusActor actor;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static UserStatusHistory byAdmin(
            Long userId, UserStatusAction statusAction, String reason,
            LocalDateTime deactivatedUntil, Long adminId
    ) {
        return UserStatusHistory.builder()
                .userId(userId)
                .statusAction(statusAction)
                .reason(reason)
                .deactivatedUntil(deactivatedUntil)
                .actor(UserStatusActor.ADMIN)
                .createdBy(adminId)
                .build();
    }

    public static UserStatusHistory bySystem(
            Long userId, UserStatusAction statusAction, String reason, LocalDateTime deactivatedUntil
    ) {
        return UserStatusHistory.builder()
                .userId(userId)
                .statusAction(statusAction)
                .reason(reason)
                .deactivatedUntil(deactivatedUntil)
                .actor(UserStatusActor.SYSTEM)
                .createdBy(null)
                .build();
    }

    public static UserStatusHistory from(UserStatusHistorySnapshotState state) {
        return UserStatusHistory.builder()
                .id(state.getId())
                .userId(state.getUserId())
                .statusAction(state.getStatusAction())
                .reason(state.getReason())
                .deactivatedUntil(state.getDeactivatedUntil())
                .actor(state.getActor())
                .createdBy(state.getCreatedBy())
                .createdAt(state.getCreatedAt())
                .build();
    }
}
