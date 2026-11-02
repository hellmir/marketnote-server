package com.personal.marketnote.community.domain.like;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LikeTest {

    @Test
    @DisplayName("LikeCreateState로 Like를 생성하면 ACTIVE 상태이다")
    void shouldCreateLikeWithActiveStatusFromCreateState() {
        LikeCreateState state = LikeCreateState.builder()
                .targetType(LikeTargetType.REVIEW)
                .targetId(1L)
                .userId(100L)
                .build();

        Like like = Like.from(state);

        assertThat(like.getTargetType()).isEqualTo(LikeTargetType.REVIEW);
        assertThat(like.getTargetId()).isEqualTo(1L);
        assertThat(like.getUserId()).isEqualTo(100L);
        assertThat(like.isLiked()).isTrue();
    }

    @Test
    @DisplayName("LikeSnapshotState로 Like를 복원하면 모든 필드가 그대로 매핑된다")
    void shouldRestoreLikeFromSnapshotState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2026, 4, 1, 12, 0);
        LikeSnapshotState state = LikeSnapshotState.builder()
                .targetType(LikeTargetType.BOARD)
                .targetId(2L)
                .userId(200L)
                .status(EntityStatus.INACTIVE)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();

        Like like = Like.from(state);

        assertThat(like.getTargetType()).isEqualTo(LikeTargetType.BOARD);
        assertThat(like.getTargetId()).isEqualTo(2L);
        assertThat(like.getUserId()).isEqualTo(200L);
        assertThat(like.isLiked()).isFalse();
        assertThat(like.getCreatedAt()).isEqualTo(createdAt);
        assertThat(like.getModifiedAt()).isEqualTo(modifiedAt);
    }

    @Test
    @DisplayName("ACTIVE 상태에서 isLiked가 false이면 상태가 변경된 것이다")
    void shouldDetectStatusChangedWhenActiveAndNotLiked() {
        Like like = createActiveLike();

        assertThat(like.isStatusChanged(false)).isTrue();
    }

    @Test
    @DisplayName("ACTIVE 상태에서 isLiked가 true이면 상태가 변경되지 않은 것이다")
    void shouldDetectStatusNotChangedWhenActiveAndLiked() {
        Like like = createActiveLike();

        assertThat(like.isStatusChanged(true)).isFalse();
    }

    @Test
    @DisplayName("INACTIVE 상태에서 isLiked가 true이면 상태가 변경된 것이다")
    void shouldDetectStatusChangedWhenInactiveAndLiked() {
        Like like = createInactiveLike();

        assertThat(like.isStatusChanged(true)).isTrue();
    }

    @Test
    @DisplayName("INACTIVE 상태에서 isLiked가 false이면 상태가 변경되지 않은 것이다")
    void shouldDetectStatusNotChangedWhenInactiveAndNotLiked() {
        Like like = createInactiveLike();

        assertThat(like.isStatusChanged(false)).isFalse();
    }

    @Test
    @DisplayName("ACTIVE 상태에서 revert를 호출하면 INACTIVE로 전환된다")
    void shouldRevertFromActiveToInactive() {
        Like like = createActiveLike();

        like.revert();

        assertThat(like.isLiked()).isFalse();
    }

    @Test
    @DisplayName("INACTIVE 상태에서 revert를 호출하면 ACTIVE로 전환된다")
    void shouldRevertFromInactiveToActive() {
        Like like = createInactiveLike();

        like.revert();

        assertThat(like.isLiked()).isTrue();
    }

    private Like createActiveLike() {
        return Like.from(LikeCreateState.builder()
                .targetType(LikeTargetType.REVIEW)
                .targetId(1L)
                .userId(100L)
                .build());
    }

    private Like createInactiveLike() {
        Like like = createActiveLike();
        like.revert();
        return like;
    }
}
