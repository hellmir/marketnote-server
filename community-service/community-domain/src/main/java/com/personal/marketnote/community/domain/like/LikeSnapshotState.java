package com.personal.marketnote.community.domain.like;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeSnapshotState {
    private final LikeTargetType targetType;
    private final Long targetId;
    private final Long userId;
    private final EntityStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}

