package com.personal.marketnote.community.domain.like;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Like {
    private LikeTargetType targetType;
    private Long targetId;
    private Long userId;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static Like from(LikeCreateState state) {
        return Like.builder()
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .userId(state.getUserId())
                .status(EntityStatus.ACTIVE)
                .build();
    }

    public static Like from(LikeSnapshotState state) {
        return Like.builder()
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .userId(state.getUserId())
                .status(state.getStatus())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public boolean isStatusChanged(boolean isLiked) {
        boolean isActive = status.isActive();
        return FormatValidator.notEquals(status.isActive(), isLiked);
    }

    public void revert() {
        status = EntityStatus.from(!isLiked());
    }

    public boolean isLiked() {
        return status.isActive();
    }
}
