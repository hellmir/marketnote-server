package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.community.domain.review.ProductReviewAggregate;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewVersionHistory;
import com.personal.marketnote.community.domain.review.ReviewVersionHistoryCreateState;
import com.personal.marketnote.community.exception.ProductReviewAggregateNotFoundException;
import com.personal.marketnote.community.mapper.ReviewCommandToStateMapper;
import com.personal.marketnote.community.port.in.command.review.RegisterReviewCommand;
import com.personal.marketnote.community.port.in.result.review.RegisterReviewResult;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.in.usecase.review.RegisterReviewUseCase;
import com.personal.marketnote.community.port.out.order.UpdateOrderProductReviewStatusPort;
import com.personal.marketnote.community.port.out.review.SaveReviewPort;
import com.personal.marketnote.community.port.out.review.UpdateReviewPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterReviewService implements RegisterReviewUseCase {
    private final GetReviewUseCase getReviewUseCase;
    private final SaveReviewPort saveReviewPort;
    private final UpdateReviewPort updateReviewPort;
    private final UpdateOrderProductReviewStatusPort updateOrderProductReviewStatusPort;

    @Override
    public RegisterReviewResult registerReview(RegisterReviewCommand command) {
        getReviewUseCase.validateDuplicateReview(command);
        Long orderId = command.orderId();
        Long productId = command.productId();
        Long pricePolicyId = command.pricePolicyId();
        Review savedReview = saveReviewPort.save(
                Review.from(ReviewCommandToStateMapper.mapToState(command))
        );

        // 리뷰 버전 기록 저장
        ReviewVersionHistoryCreateState reviewVersionHistoryCreateState
                = ReviewCommandToStateMapper.mapToVersionHistoryState(savedReview.getId(), command);
        saveReviewPort.saveVersionHistory(
                ReviewVersionHistory.from(reviewVersionHistoryCreateState)
        );

        // 상품 평점 집계
        try {
            ProductReviewAggregate productReviewAggregate = getReviewUseCase.getProductReviewAggregate(productId);
            Float point = command.rating();
            productReviewAggregate.addPoint(point.intValue());
            productReviewAggregate.computeRating(point);
            updateReviewPort.update(productReviewAggregate);
        } catch (ProductReviewAggregateNotFoundException pranfe) {
            saveReviewPort.saveAggregate(
                    ProductReviewAggregate.from(savedReview)
            );
        }

        // 주문 상품의 리뷰 작성 여부를 true로 업데이트
        // FIXME: Kafka 이벤트 Production으로 변경
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    updateOrderProductReviewStatusPort.update(orderId, pricePolicyId, true);
                }
            });
        } else {
            updateOrderProductReviewStatusPort.update(orderId, pricePolicyId, true);
        }

        return RegisterReviewResult.from(savedReview);
    }
}
