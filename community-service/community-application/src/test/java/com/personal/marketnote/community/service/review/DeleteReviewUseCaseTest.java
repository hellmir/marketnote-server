package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.domain.review.ProductReviewAggregate;
import com.personal.marketnote.community.domain.review.ProductReviewAggregateSnapshotState;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewSnapshotState;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.out.event.PublishReviewEventPort;
import com.personal.marketnote.community.port.out.review.UpdateReviewPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteReviewUseCase 테스트")
class DeleteReviewUseCaseTest {

    @InjectMocks
    private DeleteReviewService deleteReviewService;

    @Mock
    private GetReviewUseCase getReviewUseCase;

    @Mock
    private UpdateReviewPort updateReviewPort;

    @Mock
    private PublishReviewEventPort publishReviewEventPort;

    @Test
    @DisplayName("리뷰를 삭제하면 작성자 검증 후 리뷰 상태 변경, 집계 업데이트, 이벤트 발행이 순서대로 수행된다")
    void shouldDeleteReviewAndUpdateAggregateAndPublishEvent() {
        // given
        Long reviewId = 1L;
        Long reviewerId = 100L;
        Long productId = 200L;
        Float rating = 5.0f;

        Review review = buildReview(reviewId, reviewerId, productId, rating);
        ProductReviewAggregate aggregate = buildProductReviewAggregate(productId, 10, 50.0f);

        doNothing().when(getReviewUseCase).validateAuthor(reviewId, reviewerId);
        when(getReviewUseCase.getReview(reviewId)).thenReturn(review);
        when(getReviewUseCase.getProductReviewAggregate(productId)).thenReturn(aggregate);

        // when
        deleteReviewService.deleteReview(reviewId, reviewerId);

        // then
        InOrder inOrder = inOrder(getReviewUseCase, updateReviewPort, publishReviewEventPort);
        inOrder.verify(getReviewUseCase).validateAuthor(reviewId, reviewerId);
        inOrder.verify(getReviewUseCase).getReview(reviewId);
        inOrder.verify(updateReviewPort).update(review);
        inOrder.verify(getReviewUseCase).getProductReviewAggregate(productId);
        inOrder.verify(updateReviewPort).update(aggregate);
        inOrder.verify(publishReviewEventPort).publishReviewDeletedEvent(
                eq(reviewId), eq(productId),
                eq(aggregate.getTotalCount()), eq(aggregate.getAverageRating())
        );
    }

    private Review buildReview(Long id, Long reviewerId, Long productId, Float rating) {
        return Review.from(
                ReviewSnapshotState.builder()
                        .id(id)
                        .reviewerId(reviewerId)
                        .orderId(1000L)
                        .productId(productId)
                        .pricePolicyId(3000L)
                        .productImageUrl("https://example.com/product.jpg")
                        .selectedOptions("옵션")
                        .quantity(1)
                        .reviewerName("작성자")
                        .maskedReviewerName("작*자")
                        .rating(rating)
                        .content("리뷰 내용")
                        .isPhoto(false)
                        .isEdited(false)
                        .likeCount(0)
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .orderNum(id)
                        .build()
        );
    }

    private ProductReviewAggregate buildProductReviewAggregate(Long productId, int totalCount, float totalRating) {
        return ProductReviewAggregate.from(
                ProductReviewAggregateSnapshotState.builder()
                        .productId(productId)
                        .totalCount(totalCount)
                        .fivePointCount(totalCount)
                        .fourPointCount(0)
                        .threePointCount(0)
                        .twoPointCount(0)
                        .onePointCount(0)
                        .totalRating(totalRating)
                        .averageRating(totalRating / totalCount)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }
}
