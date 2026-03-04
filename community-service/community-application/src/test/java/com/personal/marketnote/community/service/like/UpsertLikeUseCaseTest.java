package com.personal.marketnote.community.service.like;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.community.domain.like.Like;
import com.personal.marketnote.community.domain.like.LikeSnapshotState;
import com.personal.marketnote.community.domain.like.LikeTargetType;
import com.personal.marketnote.community.exception.LikeNotFoundException;
import com.personal.marketnote.community.port.in.command.like.UpsertLikeCommand;
import com.personal.marketnote.community.port.in.result.like.UpsertLikeResult;
import com.personal.marketnote.community.port.in.usecase.like.GetLikeUseCase;
import com.personal.marketnote.community.port.out.like.SaveLikePort;
import com.personal.marketnote.community.port.out.like.UpdateLikePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpsertLikeUseCaseTest {
    @Mock
    private GetLikeUseCase getLikeUseCase;
    @Mock
    private SaveLikePort saveLikePort;
    @Mock
    private UpdateLikePort updateLikePort;

    @InjectMocks
    private UpsertLikeService upsertLikeService;

    @Test
    @DisplayName("좋아요가 존재하지 않으면 신규 Like를 생성한다")
    void upsertLike_likeNotExists_createsNewLike() {
        LikeTargetType targetType = LikeTargetType.REVIEW;
        Long targetId = 1L;
        Long userId = 1L;
        UpsertLikeCommand command = UpsertLikeCommand.of(targetType, targetId, true, userId);
        when(getLikeUseCase.getLike(targetType, targetId, userId))
                .thenThrow(new LikeNotFoundException(targetType, targetId, userId));

        UpsertLikeResult result = upsertLikeService.upsertLike(command);

        assertThat(result.isNew()).isTrue();
        assertThat(result.isLiked()).isTrue();
        verify(saveLikePort).save(any(Like.class));
        verifyNoInteractions(updateLikePort);
    }

    @Test
    @DisplayName("좋아요가 ACTIVE 상태이고 isLiked가 false이면 INACTIVE로 변경한다")
    void upsertLike_activeLikeAndUnlike_revertsToInactive() {
        LikeTargetType targetType = LikeTargetType.REVIEW;
        Long targetId = 1L;
        Long userId = 1L;
        Like like = buildLike(targetType, targetId, userId, EntityStatus.ACTIVE);
        UpsertLikeCommand command = UpsertLikeCommand.of(targetType, targetId, false, userId);
        when(getLikeUseCase.getLike(targetType, targetId, userId)).thenReturn(like);

        UpsertLikeResult result = upsertLikeService.upsertLike(command);

        assertThat(result.isNew()).isFalse();
        assertThat(result.isLiked()).isFalse();
        verify(updateLikePort).update(like);
        verifyNoInteractions(saveLikePort);
    }

    @Test
    @DisplayName("좋아요가 INACTIVE 상태이고 isLiked가 true이면 ACTIVE로 변경한다")
    void upsertLike_inactiveLikeAndLike_revertsToActive() {
        LikeTargetType targetType = LikeTargetType.BOARD;
        Long targetId = 2L;
        Long userId = 3L;
        Like like = buildLike(targetType, targetId, userId, EntityStatus.INACTIVE);
        UpsertLikeCommand command = UpsertLikeCommand.of(targetType, targetId, true, userId);
        when(getLikeUseCase.getLike(targetType, targetId, userId)).thenReturn(like);

        UpsertLikeResult result = upsertLikeService.upsertLike(command);

        assertThat(result.isNew()).isFalse();
        assertThat(result.isLiked()).isTrue();
        verify(updateLikePort).update(like);
        verifyNoInteractions(saveLikePort);
    }

    @Test
    @DisplayName("좋아요 상태가 동일하면 updateLikePort를 호출하지 않는다")
    void upsertLike_sameStatus_doesNotCallUpdatePort() {
        LikeTargetType targetType = LikeTargetType.REVIEW;
        Long targetId = 1L;
        Long userId = 1L;
        Like like = buildLike(targetType, targetId, userId, EntityStatus.ACTIVE);
        UpsertLikeCommand command = UpsertLikeCommand.of(targetType, targetId, true, userId);
        when(getLikeUseCase.getLike(targetType, targetId, userId)).thenReturn(like);

        UpsertLikeResult result = upsertLikeService.upsertLike(command);

        assertThat(result.isNew()).isFalse();
        assertThat(result.isLiked()).isTrue();
        verifyNoInteractions(updateLikePort);
        verifyNoInteractions(saveLikePort);
    }

    @Test
    @DisplayName("신규 생성 시 saveLikePort.save()가 호출된다")
    void upsertLike_newLike_callsSavePort() {
        LikeTargetType targetType = LikeTargetType.BOARD;
        Long targetId = 5L;
        Long userId = 10L;
        UpsertLikeCommand command = UpsertLikeCommand.of(targetType, targetId, true, userId);
        when(getLikeUseCase.getLike(targetType, targetId, userId))
                .thenThrow(new LikeNotFoundException(targetType, targetId, userId));

        upsertLikeService.upsertLike(command);

        verify(saveLikePort).save(any(Like.class));
        verify(getLikeUseCase).getLike(targetType, targetId, userId);
        verifyNoInteractions(updateLikePort);
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
