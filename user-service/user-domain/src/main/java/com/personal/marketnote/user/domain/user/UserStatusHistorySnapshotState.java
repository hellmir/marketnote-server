package com.personal.marketnote.user.domain.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserStatusHistorySnapshotState {
    private final Long id;
    private final Long userId;
    private final UserStatusAction statusAction;
    private final String reason;
    private final LocalDateTime deactivatedUntil;
    private final UserStatusActor actor;
    private final Long createdBy;
    private final LocalDateTime createdAt;
}
