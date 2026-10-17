package com.personal.marketnote.community.service.report;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.post.PostSnapshotState;
import com.personal.marketnote.community.domain.report.ReportTargetType;
import com.personal.marketnote.community.domain.review.Rating;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewSnapshotState;
import com.personal.marketnote.community.exception.PostNotFoundException;
import com.personal.marketnote.community.exception.ReviewNotFoundException;
import com.personal.marketnote.community.port.in.command.report.UpdateTargetStatusCommand;
import com.personal.marketnote.community.port.in.usecase.post.GetPostUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.out.post.FindPostPort;
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
    private FindPostPort findPostPort;
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
        verifyNoInteractions(getReviewUseCase, updateReviewPort, findPostPort);
    }

    @Test
    @DisplayName("비노출 상태의 게시글을 복구 요청하면 노출 상태가 변경된다")
    void updateTargetStatus_restoreUnexposedPost_changesExposure() {
        Post post = buildPost(11L, EntityStatus.UNEXPOSED);
        UpdateTargetStatusCommand command = UpdateTargetStatusCommand.of(ReportTargetType.POST, 11L, true);
        when(getPostUseCase.getPost(11L)).thenReturn(post);

        updateTargetStatusService.updateTargetStatus(command);

        verify(updatePostPort).update(post);
        verifyNoInteractions(getReviewUseCase, updateReviewPort, findPostPort);
    }

    @Test
    @DisplayName("이미 활성화 상태의 게시글에 노출 요청하면 상태를 변경하지 않는다")
    void updateTargetStatus_alreadyActivePost_doesNotUpdate() {
        Post post = buildPost(12L, EntityStatus.ACTIVE);
        UpdateTargetStatusCommand command = UpdateTargetStatusCommand.of(ReportTargetType.POST, 12L, true);
        when(getPostUseCase.getPost(12L)).thenReturn(post);

        updateTargetStatusService.updateTargetStatus(command);

        verifyNoInteractions(updatePostPort, getReviewUseCase, updateReviewPort, findPostPort);
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

        verifyNoInteractions(updatePostPort, getReviewUseCase, updateReviewPort, findPostPort);
    }

    private Review buildReview(Long id, EntityStatus status) {
        return Review.from(ReviewSnapshotState.builder()
                .id(id)
                .reviewerId(100L)
                .orderId(200L)
                .productId(300L)
                .pricePolicyId(400L)
                .rating(Rating.of(5.0f))
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

    private Post buildReply(Long id, Long parentId, EntityStatus status) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(200L)
                .parentId(parentId)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .title("답변 제목")
                .content("답변 내용")
                .isPrivate(false)
                .isPhoto(false)
                .status(status)
                .build());
    }

    private Post buildParent(Long id, boolean hasReply) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(100L)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .title("문의 제목")
                .content("문의 내용")
                .isPrivate(false)
                .isPhoto(false)
                .hasReply(hasReply)
                .status(EntityStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("답글을 숨기기 요청해 부모에 활성 답글이 남지 않으면 부모의 hasReply가 false로 재계산된다")
    void updateTargetStatus_hideReply_recalculatesParentHasReplyToFalse() {
        Long replyId = 500L;
        Long parentId = 600L;
        Post reply = buildReply(replyId, parentId, EntityStatus.ACTIVE);
        Post parent = buildParent(parentId, true);
        UpdateTargetStatusCommand command = UpdateTargetStatusCommand.of(ReportTargetType.POST, replyId, false);
        when(getPostUseCase.getPost(replyId)).thenReturn(reply);
        when(getPostUseCase.getPost(parentId)).thenReturn(parent);
        when(findPostPort.existsActiveReplyByParentId(parentId)).thenReturn(false);

        updateTargetStatusService.updateTargetStatus(command);

        verify(updatePostPort).update(reply);
        verify(updatePostPort).update(parent);
        org.assertj.core.api.Assertions.assertThat(parent.hasReply()).isFalse();
    }

    @Test
    @DisplayName("답글을 숨기기 요청해도 부모에 활성 답글이 남으면 부모의 hasReply가 유지되고 parent update를 skip한다")
    void updateTargetStatus_hideReply_keepsParentHasReplyWhenOtherActiveRepliesExist() {
        Long replyId = 501L;
        Long parentId = 601L;
        Post reply = buildReply(replyId, parentId, EntityStatus.ACTIVE);
        Post parent = buildParent(parentId, true);
        UpdateTargetStatusCommand command = UpdateTargetStatusCommand.of(ReportTargetType.POST, replyId, false);
        when(getPostUseCase.getPost(replyId)).thenReturn(reply);
        when(getPostUseCase.getPost(parentId)).thenReturn(parent);
        when(findPostPort.existsActiveReplyByParentId(parentId)).thenReturn(true);

        updateTargetStatusService.updateTargetStatus(command);

        verify(updatePostPort).update(reply);
        verify(updatePostPort, never()).update(parent);
    }

    @Test
    @DisplayName("비노출 답글을 복구 요청해 부모 활성 답글이 생기면 부모의 hasReply가 true로 재계산된다")
    void updateTargetStatus_restoreReply_recalculatesParentHasReplyToTrue() {
        Long replyId = 502L;
        Long parentId = 602L;
        Post reply = buildReply(replyId, parentId, EntityStatus.UNEXPOSED);
        Post parent = buildParent(parentId, false);
        UpdateTargetStatusCommand command = UpdateTargetStatusCommand.of(ReportTargetType.POST, replyId, true);
        when(getPostUseCase.getPost(replyId)).thenReturn(reply);
        when(getPostUseCase.getPost(parentId)).thenReturn(parent);
        when(findPostPort.existsActiveReplyByParentId(parentId)).thenReturn(true);

        updateTargetStatusService.updateTargetStatus(command);

        verify(updatePostPort).update(reply);
        verify(updatePostPort).update(parent);
        org.assertj.core.api.Assertions.assertThat(parent.hasReply()).isTrue();
    }

    @Test
    @DisplayName("일반 게시글(답글 아님) status 변경 시 부모 hasReply 재계산 로직이 실행되지 않는다")
    void updateTargetStatus_nonReplyPost_skipsParentRecalculation() {
        Post post = buildPost(700L, EntityStatus.ACTIVE);
        UpdateTargetStatusCommand command = UpdateTargetStatusCommand.of(ReportTargetType.POST, 700L, false);
        when(getPostUseCase.getPost(700L)).thenReturn(post);

        updateTargetStatusService.updateTargetStatus(command);

        verify(updatePostPort).update(post);
        verifyNoInteractions(findPostPort);
    }
}
