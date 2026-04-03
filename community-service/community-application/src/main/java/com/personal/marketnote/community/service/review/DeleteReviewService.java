package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.community.domain.review.ProductReviewAggregate;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.port.in.usecase.review.DeleteReviewUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.out.event.PublishReviewEventPort;
import com.personal.marketnote.community.port.out.review.UpdateReviewPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class DeleteReviewService implements DeleteReviewUseCase {
    private final GetReviewUseCase getReviewUseCase;
    private final UpdateReviewPort updateReviewPort;
    private final PublishReviewEventPort publishReviewEventPort;

    @Override
    public void deleteReview(Long id, Long reviewerId) {
        getReviewUseCase.validateAuthor(id, reviewerId);
        Review review = getReviewUseCase.getReview(id);
        Float rating = review.getRating();
        Long productId = review.getProductId();
        review.delete();
        updateReviewPort.update(review);

        // 상품 평점 집계 업데이트
        ProductReviewAggregate productReviewAggregate = getReviewUseCase.getProductReviewAggregate(productId);
        productReviewAggregate.reducePoint(rating.intValue());
        productReviewAggregate.computeRating(-rating);
        updateReviewPort.update(productReviewAggregate);

        // 리뷰 삭제 이벤트 발행
        publishReviewEventPort.publishReviewDeletedEvent(
                review.getId(), productId,
                productReviewAggregate.getTotalCount(), productReviewAggregate.getAverageRating()
        );
    }
}
