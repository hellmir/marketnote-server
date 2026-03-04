package com.personal.marketnote.community.service.like;

import com.personal.marketnote.community.domain.like.LikeTargetType;
import com.personal.marketnote.community.port.out.like.FindLikePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExistsUserLikeUseCaseTest {
    @Mock
    private FindLikePort findLikePort;

    @InjectMocks
    private GetLikeService getLikeService;

    @Test
    @DisplayName("좋아요가 존재하면 true를 반환한다")
    void existsUserLike_likeExists_returnsTrue() {
        LikeTargetType targetType = LikeTargetType.REVIEW;
        Long targetId = 1L;
        Long userId = 1L;
        when(findLikePort.existsByTargetAndUser(targetType, targetId, userId)).thenReturn(true);

        boolean result = getLikeService.existsUserLike(targetType, targetId, userId);

        assertThat(result).isTrue();
        verify(findLikePort).existsByTargetAndUser(targetType, targetId, userId);
    }

    @Test
    @DisplayName("좋아요가 존재하지 않으면 false를 반환한다")
    void existsUserLike_likeNotExists_returnsFalse() {
        LikeTargetType targetType = LikeTargetType.BOARD;
        Long targetId = 2L;
        Long userId = 3L;
        when(findLikePort.existsByTargetAndUser(targetType, targetId, userId)).thenReturn(false);

        boolean result = getLikeService.existsUserLike(targetType, targetId, userId);

        assertThat(result).isFalse();
        verify(findLikePort).existsByTargetAndUser(targetType, targetId, userId);
    }
}
