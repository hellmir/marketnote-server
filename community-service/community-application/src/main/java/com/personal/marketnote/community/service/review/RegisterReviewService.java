package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.review.ProductReviewAggregate;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewVersionHistory;
import com.personal.marketnote.community.domain.review.ReviewVersionHistoryCreateState;
import com.personal.marketnote.community.exception.InvalidReviewContentContainsProfanityException;
import com.personal.marketnote.community.exception.ProductReviewAggregateNotFoundException;
import com.personal.marketnote.community.mapper.ReviewCommandToStateMapper;
import com.personal.marketnote.community.port.in.command.review.RegisterReviewCommand;
import com.personal.marketnote.community.port.in.result.review.RegisterReviewResult;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.in.usecase.review.RegisterReviewUseCase;
import com.personal.marketnote.community.port.out.event.PublishReviewEventPort;
import com.personal.marketnote.community.port.out.profanity.FindProfanityWordPort;
import com.personal.marketnote.community.port.out.review.SaveReviewPort;
import com.personal.marketnote.community.port.out.review.UpdateReviewPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterReviewService implements RegisterReviewUseCase {
    private final GetReviewUseCase getReviewUseCase;
    private final SaveReviewPort saveReviewPort;
    private final UpdateReviewPort updateReviewPort;
    private final PublishReviewEventPort publishReviewEventPort;
    private final FindProfanityWordPort findProfanityWordPort;

    @Override
    public RegisterReviewResult registerReview(RegisterReviewCommand command) {
        validateProfanity(command.content());
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
        ProductReviewAggregate productReviewAggregate;
        try {
            productReviewAggregate = getReviewUseCase.getProductReviewAggregate(productId);
            Float point = command.rating();
            productReviewAggregate.addPoint(point.intValue());
            productReviewAggregate.computeRating(point);
            updateReviewPort.update(productReviewAggregate);
        } catch (ProductReviewAggregateNotFoundException pranfe) {
            productReviewAggregate = ProductReviewAggregate.from(savedReview);
            saveReviewPort.saveAggregate(productReviewAggregate);
        }

        // Outbox 이벤트 저장 (트랜잭션 내)
        // [#929][#1038] 주문상품 리뷰 상태 업데이트는 Kafka Consumer(ReviewRegisteredCommerceConsumer)로 전환 완료
        publishReviewEventPort.publishReviewRegisteredEvent(
                orderId, pricePolicyId, productId,
                productReviewAggregate.getTotalCount(), productReviewAggregate.getAverageRating()
        );

        return RegisterReviewResult.from(savedReview);
    }

    private void validateProfanity(String content) {
        if (FormatValidator.hasValue(content) && findProfanityWordPort.containsProfanity(content)) {
            throw new InvalidReviewContentContainsProfanityException();
        }
    }
}
