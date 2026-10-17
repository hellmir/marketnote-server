package com.personal.marketnote.community.service.report;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.port.in.command.report.UpdateTargetStatusCommand;
import com.personal.marketnote.community.port.in.usecase.post.GetPostUseCase;
import com.personal.marketnote.community.port.in.usecase.report.UpdateTargetStatusUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.out.post.FindPostPort;
import com.personal.marketnote.community.port.out.post.UpdatePostPort;
import com.personal.marketnote.community.port.out.review.UpdateReviewPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpdateTargetStatusService implements UpdateTargetStatusUseCase {
    private final GetReviewUseCase getReviewUseCase;
    private final GetPostUseCase getPostUseCase;
    private final FindPostPort findPostPort;
    private final UpdateReviewPort updateReviewPort;
    private final UpdatePostPort updatePostPort;

    @Override
    public void updateTargetStatus(UpdateTargetStatusCommand command) {
        boolean isVisible = command.isVisible();

        if (command.isReview()) {
            Review review = getReviewUseCase.getReview(command.targetId());
            if (review.isStatusChanged(isVisible)) {
                review.changeExposure();
                updateReviewPort.update(review);
            }

            return;
        }

        Post post = getPostUseCase.getPost(command.targetId());
        if (!post.isStatusChanged(isVisible)) {
            return;
        }
        post.changeExposure();
        updatePostPort.update(post);
        if (post.isReply()) {
            recalculateParentHasReply(post.getParentId());
        }
    }

    private void recalculateParentHasReply(Long parentId) {
        Post parent = getPostUseCase.getPost(parentId);
        boolean hasActiveReply = findPostPort.existsActiveReplyByParentId(parentId);
        if (!parent.isHasReplyChanged(hasActiveReply)) {
            return;
        }
        parent.applyHasReply(hasActiveReply);
        updatePostPort.update(parent);
    }
}
