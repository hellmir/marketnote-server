package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.exception.ReviewNotFoundException;
import com.personal.marketnote.community.port.in.result.review.GetReviewKeyResult;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewKeyUseCase;
import com.personal.marketnote.community.port.out.review.FindReviewPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetReviewKeyService implements GetReviewKeyUseCase {
    private final FindReviewPort findReviewPort;

    @Override
    public GetReviewKeyResult getReviewKey(Long reviewId, Long userId) {
        Review review = findReviewPort.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (!review.isOwnedBy(userId)) {
            throw new ReviewNotFoundException(reviewId);
        }

        return GetReviewKeyResult.from(review);
    }
}
