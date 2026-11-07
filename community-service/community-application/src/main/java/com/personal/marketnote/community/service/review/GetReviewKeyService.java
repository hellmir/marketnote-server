package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.exception.ReviewNotFoundException;
import com.personal.marketnote.community.port.in.result.review.GetReviewKeyResult;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewKeyUseCase;
import com.personal.marketnote.community.port.out.review.FindReviewPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetReviewKeyService implements GetReviewKeyUseCase {
    private final FindReviewPort findReviewPort;

    @Override
    public GetReviewKeyResult getReviewKey(Long reviewId, Long userId) {
        Review review = findReviewPort.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("리뷰를 찾을 수 없습니다. reviewId={}", reviewId);
                    return new ReviewNotFoundException();
                });

        if (!review.isOwnedBy(userId)) {
            log.warn("리뷰 소유자 불일치로 접근 거부. reviewId={}, userId={}", reviewId, userId);
            throw new ReviewNotFoundException();
        }

        return GetReviewKeyResult.from(review);
    }
}
