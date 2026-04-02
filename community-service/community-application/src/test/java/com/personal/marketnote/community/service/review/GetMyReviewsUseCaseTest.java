package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewSnapshotState;
import com.personal.marketnote.community.domain.review.ReviewSortProperty;
import com.personal.marketnote.community.domain.review.Reviews;
import com.personal.marketnote.community.port.in.result.review.GetMyReviewsResult;
import com.personal.marketnote.community.port.in.result.review.MyReviewItemResult;
import com.personal.marketnote.community.port.in.usecase.like.GetLikeUseCase;
import com.personal.marketnote.community.port.out.file.FindReviewImagesPort;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.community.port.out.review.FindReviewPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMyReviewsUseCaseTest {
    @Mock
    private FindReviewPort findReviewPort;
    @Mock
    private FindReviewImagesPort findReviewImagesPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;
    @Mock
    private GetLikeUseCase getLikeUseCase;

    @InjectMocks
    private GetReviewService getReviewService;

    @Test
    @DisplayName("나의 리뷰 내역 조회 시 응답에 reviewerName이 포함된다")
    void getWriterReviews_containsReviewerName() {
        Long userId = 10L;
        Review review = buildReview(101L, userId, 20L, 1001L, false);
        Reviews reviews = Reviews.from(List.of(review));

        when(findReviewPort.findUserReviews(eq(userId), eq(-1L), any(Pageable.class), eq(ReviewSortProperty.ID)))
                .thenReturn(reviews);
        when(findReviewPort.countActive(userId)).thenReturn(1L);

        GetMyReviewsResult result = getReviewService.getWriterReviews(
                userId, -1L, 4, Sort.Direction.DESC, ReviewSortProperty.ID
        );

        assertThat(result.reviews()).hasSize(1);
        MyReviewItemResult item = result.reviews().getFirst();
        assertThat(item.reviewerName()).isEqualTo("사용자-101");
    }

    @Test
    @DisplayName("나의 리뷰 내역 조회 시 응답에 reviewerMaskedName이 포함되지 않는다")
    void getWriterReviews_doesNotContainReviewerMaskedName() {
        Long userId = 10L;
        Review review = buildReview(101L, userId, 20L, 1001L, false);
        Reviews reviews = Reviews.from(List.of(review));

        when(findReviewPort.findUserReviews(eq(userId), eq(-1L), any(Pageable.class), eq(ReviewSortProperty.ID)))
                .thenReturn(reviews);
        when(findReviewPort.countActive(userId)).thenReturn(1L);

        GetMyReviewsResult result = getReviewService.getWriterReviews(
                userId, -1L, 4, Sort.Direction.DESC, ReviewSortProperty.ID
        );

        assertThat(result.reviews()).hasSize(1);
        MyReviewItemResult item = result.reviews().getFirst();
        // MyReviewItemResult에는 reviewerMaskedName 필드가 존재하지 않음을 컴파일 타임에 보장
        // reviewerName만 포함되어 있는지 검증
        assertThat(item.reviewerName()).isNotNull();
        assertThat(item).hasNoNullFieldsOrPropertiesExcept("images", "product");
    }

    private Review buildReview(Long id, Long reviewerId, Long productId, Long pricePolicyId, boolean isPhoto) {
        return Review.from(
                ReviewSnapshotState.builder()
                        .id(id)
                        .reviewerId(reviewerId)
                        .orderId(1000L + id)
                        .productId(productId)
                        .pricePolicyId(pricePolicyId)
                        .productImageUrl("https://example.com/product-" + productId + ".jpg")
                        .selectedOptions("옵션-" + id)
                        .quantity(1)
                        .reviewerName("사용자-" + id)
                        .reviewerMaskedName("사*자-" + id)
                        .rating(5.0f)
                        .content("리뷰-" + id)
                        .isPhoto(isPhoto)
                        .isEdited(false)
                        .likeCount(3)
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .orderNum(id)
                        .build()
        );
    }
}
