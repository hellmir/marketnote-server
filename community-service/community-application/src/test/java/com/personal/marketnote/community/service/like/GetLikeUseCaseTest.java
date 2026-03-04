package com.personal.marketnote.community.service.like;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.community.domain.like.Like;
import com.personal.marketnote.community.domain.like.LikeSnapshotState;
import com.personal.marketnote.community.domain.like.LikeTargetType;
import com.personal.marketnote.community.exception.LikeNotFoundException;
import com.personal.marketnote.community.port.out.like.FindLikePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetLikeUseCaseTest {
    @Mock
    private FindLikePort findLikePort;

    @InjectMocks
    private GetLikeService getLikeService;

    @Test
    @DisplayName("좋아요가 존재하면 Like 객체를 반환한다")
    void getLike_likeExists_returnsLike() {
        LikeTargetType targetType = LikeTargetType.REVIEW;
        Long targetId = 1L;
        Long userId = 1L;
        Like like = buildLike(targetType, targetId, userId, EntityStatus.ACTIVE);
        when(findLikePort.findByTargetAndUser(targetType, targetId, userId))
                .thenReturn(Optional.of(like));

        Like result = getLikeService.getLike(targetType, targetId, userId);

        assertThat(result).isNotNull();
        assertThat(result.getTargetType()).isEqualTo(targetType);
        assertThat(result.getTargetId()).isEqualTo(targetId);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.isLiked()).isTrue();
        verify(findLikePort).findByTargetAndUser(targetType, targetId, userId);
    }

    @Test
    @DisplayName("좋아요가 존재하지 않으면 LikeNotFoundException을 발생시킨다")
    void getLike_likeNotExists_throwsLikeNotFoundException() {
        LikeTargetType targetType = LikeTargetType.BOARD;
        Long targetId = 2L;
        Long userId = 3L;
        when(findLikePort.findByTargetAndUser(targetType, targetId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> getLikeService.getLike(targetType, targetId, userId))
                .isInstanceOf(LikeNotFoundException.class)
                .hasMessageContaining(String.valueOf(targetId))
                .hasMessageContaining(String.valueOf(userId));
    }

    private Like buildLike(LikeTargetType targetType, Long targetId, Long userId, EntityStatus status) {
        return Like.from(LikeSnapshotState.builder()
                .targetType(targetType)
                .targetId(targetId)
                .userId(userId)
                .status(status)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
