package com.personal.marketnote.community.service.report;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.post.PostSnapshotState;
import com.personal.marketnote.community.domain.report.ReportTargetType;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewSnapshotState;
import com.personal.marketnote.community.exception.PostNotFoundException;
import com.personal.marketnote.community.exception.ReviewNotFoundException;
import com.personal.marketnote.community.port.in.command.report.UpdateTargetStatusCommand;
import com.personal.marketnote.community.port.in.usecase.post.GetPostUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.out.post.UpdatePostPort;
import com.personal.marketnote.community.port.out.review.UpdateReviewPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTargetStatusUseCaseTest {
    @Mock
    private GetReviewUseCase getReviewUseCase;
    @Mock
    private GetPostUseCase getPostUseCase;
    @Mock
    private UpdateReviewPort updateReviewPort;
    @Mock
    private UpdatePostPort updatePostPort;

    @InjectMocks
    private UpdateTargetStatusService updateTargetStatusService;

    @Test
    @DisplayName("활성화 상태의 리뷰를 숨기기 요청하면 노출 상태가 변경된다")
    void updateTargetStatus_hideActiveReview_changesExposure() {
        Review review = buildReview(1L, EntityStatus.ACTIVE);
        UpdateTargetStatusCommand command = UpdateTargetStatusCommand.of(ReportTargetType.REVIEW, 1L, false);
        when(getReviewUseCase.getReview(1L)).thenReturn(review);

        updateTargetStatusService.updateTargetStatus(command);

        verify(updateReviewPort).update(review);
        verifyNoInteractions(getPostUseCase, updatePostPort);
    }

    @Test
    @DisplayName("비노출 상태의 리뷰를 복구 요청하면 노출 상태가 변경된다")
    void updateTargetStatus_restoreUnexposedReview_changesExposure() {
        Review review = buildReview(2L, EntityStatus.UNEXPOSED);
        UpdateTargetStatusCommand command = UpdateTargetStatusCommand.of(ReportTargetType.REVIEW, 2L, true);
        when(getReviewUseCase.getReview(2L)).thenReturn(review);

        updateTargetStatusService.updateTargetStatus(command);

        verify(updateReviewPort).update(review);
        verifyNoInteractions(getPostUseCase, updatePostPort);
    }

    @Test
    @DisplayName("이미 활성화 상태의 리뷰에 노출 요청하면 상태를 변경하지 않는다")
    void updateTargetStatus_alreadyActiveReview_doesNotUpdate() {
        Review review = buildReview(3L, EntityStatus.ACTIVE);
        UpdateTargetStatusCommand command = UpdateTargetStatusCommand.of(ReportTargetType.REVIEW, 3L, true);
        when(getReviewUseCase.getReview(3L)).thenReturn(review);

        updateTargetStatusService.updateTargetStatus(command);

        verifyNoInteractions(updateReviewPort, getPostUseCase, updatePostPort);
    }

    @Test
    @DisplayName("활성화 상태의 게시글을 숨기기 요청하면 노출 상태가 변경된다")
    void updateTargetStatus_hideActivePost_changesExposure() {
        Post post = buildPost(10L, EntityStatus.ACTIVE);
        UpdateTargetStatusCommand command = UpdateTargetStatusCommand.of(ReportTargetType.POST, 10L, false);
        when(getPostUseCase.getPost(10L)).thenReturn(post);

        updateTargetStatusService.updateTargetStatus(command);

        verify(updatePostPort).update(post);
        verifyNoInteractions(getReviewUseCase, updateReviewPort);
    }

    @Test
    @DisplayName("비노출 상태의 게시글을 복구 요청하면 노출 상태가 변경된다")
    void updateTargetStatus_restoreUnexposedPost_changesExposure() {
        Post post = buildPost(11L, EntityStatus.UNEXPOSED);
        UpdateTargetStatusCommand command = UpdateTargetStatusCommand.of(ReportTargetType.POST, 11L, true);
        when(getPostUseCase.getPost(11L)).thenReturn(post);

        updateTargetStatusService.updateTargetStatus(command);

        verify(updatePostPort).update(post);
        verifyNoInteractions(getReviewUseCase, updateReviewPort);
    }

    @Test
    @DisplayName("이미 활성화 상태의 게시글에 노출 요청하면 상태를 변경하지 않는다")
    void updateTargetStatus_alreadyActivePost_doesNotUpdate() {
        Post post = buildPost(12L, EntityStatus.ACTIVE);
        UpdateTargetStatusCommand command = UpdateTargetStatusCommand.of(ReportTargetType.POST, 12L, true);
        when(getPostUseCase.getPost(12L)).thenReturn(post);

        updateTargetStatusService.updateTargetStatus(command);

        verifyNoInteractions(updatePostPort, getReviewUseCase, updateReviewPort);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰의 노출 상태를 변경하면 ReviewNotFoundException이 발생한다")
    void updateTargetStatus_reviewNotFound_throwsReviewNotFoundException() {
        UpdateTargetStatusCommand command = UpdateTargetStatusCommand.of(ReportTargetType.REVIEW, 999L, false);
        when(getReviewUseCase.getReview(999L)).thenThrow(new ReviewNotFoundException(999L));

        assertThatThrownBy(() -> updateTargetStatusService.updateTargetStatus(command))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessageContaining("999");

        verifyNoInteractions(updateReviewPort, getPostUseCase, updatePostPort);
    }

    @Test
    @DisplayName("존재하지 않는 게시글의 노출 상태를 변경하면 PostNotFoundException이 발생한다")
    void updateTargetStatus_postNotFound_throwsPostNotFoundException() {
        UpdateTargetStatusCommand command = UpdateTargetStatusCommand.of(ReportTargetType.POST, 999L, false);
        when(getPostUseCase.getPost(999L)).thenThrow(new PostNotFoundException(999L));

        assertThatThrownBy(() -> updateTargetStatusService.updateTargetStatus(command))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("999");

        verifyNoInteractions(updatePostPort, getReviewUseCase, updateReviewPort);
    }

    private Review buildReview(Long id, EntityStatus status) {
        return Review.from(ReviewSnapshotState.builder()
                .id(id)
                .reviewerId(100L)
                .orderId(200L)
                .productId(300L)
                .pricePolicyId(400L)
                .rating(5.0f)
                .content("테스트 리뷰")
                .isPhoto(false)
                .isEdited(false)
                .likeCount(0)
                .status(status)
                .build());
    }

    private Post buildPost(Long id, EntityStatus status) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(100L)
                .board(Board.NOTICE)
                .category("ANNOUNCEMENT")
                .title("테스트 게시글")
                .content("테스트 내용")
                .isPrivate(false)
                .isPhoto(false)
                .status(status)
                .build());
    }
}
