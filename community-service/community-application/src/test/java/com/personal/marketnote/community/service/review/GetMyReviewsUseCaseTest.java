package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewSnapshotState;
import com.personal.marketnote.community.domain.review.ReviewSortProperty;
import com.personal.marketnote.community.domain.review.Reviews;
import com.personal.marketnote.community.port.in.result.review.GetMyReviewsResult;
import com.personal.marketnote.community.port.in.result.review.MyReviewItemResult;
import com.personal.marketnote.community.port.in.usecase.like.GetLikeUseCase;
import com.personal.marketnote.community.port.out.file.FindReviewImagesPort;
import com.personal.marketnote.community.port.out.order.FindOrderProductPort;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.community.port.out.result.product.ProductPricePolicyInfoResult;
import com.personal.marketnote.community.port.out.review.FindReviewPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    private FindOrderProductPort findOrderProductPort;
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
    @DisplayName("나의 리뷰 내역 조회 시 응답에 maskedReviewerName이 포함되지 않는다")
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
        // MyReviewItemResult에는 maskedReviewerName 필드가 존재하지 않음을 컴파일 타임에 보장
        // reviewerName만 포함되어 있는지 검증
        assertThat(item.reviewerName()).isNotNull();
        assertThat(item).hasNoNullFieldsOrPropertiesExcept("images", "product");
    }

    // ========== unitAmount ==========

    @Test
    @DisplayName("나의 리뷰 목록 조회 시 Review에 unitAmount가 저장되어 있으면 해당 값이 응답의 product.unitAmount에 포함된다")
    void getWriterReviews_withUnitAmount_includesUnitAmountInProductInfo() {
        // given
        Long userId = 10L;
        Long pricePolicyId = 1001L;
        Long unitAmount = 15000L;
        Review review = buildReviewWithUnitAmount(101L, userId, 20L, pricePolicyId, false, unitAmount);
        Reviews reviews = Reviews.from(List.of(review));

        when(findReviewPort.findUserReviews(eq(userId), eq(-1L), any(Pageable.class), eq(ReviewSortProperty.ID)))
                .thenReturn(reviews);
        when(findReviewPort.countActive(userId)).thenReturn(1L);

        ProductInfoResult productInfo = buildProductInfo(pricePolicyId);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, productInfo));

        // when
        GetMyReviewsResult result = getReviewService.getWriterReviews(
                userId, -1L, 4, Sort.Direction.DESC, ReviewSortProperty.ID
        );

        // then
        assertThat(result.reviews()).hasSize(1);
        assertThat(result.reviews().getFirst().product()).isNotNull();
        assertThat(result.reviews().getFirst().product().unitAmount()).isEqualTo(unitAmount);
    }

    @Test
    @DisplayName("나의 리뷰 목록 조회 시 Review에 unitAmount가 null이면 응답의 product.unitAmount가 null이다")
    void getWriterReviews_withoutUnitAmount_returnsNullUnitAmount() {
        // given
        Long userId = 10L;
        Long pricePolicyId = 1001L;
        Review review = buildReview(101L, userId, 20L, pricePolicyId, false);
        Reviews reviews = Reviews.from(List.of(review));

        when(findReviewPort.findUserReviews(eq(userId), eq(-1L), any(Pageable.class), eq(ReviewSortProperty.ID)))
                .thenReturn(reviews);
        when(findReviewPort.countActive(userId)).thenReturn(1L);

        ProductInfoResult productInfo = buildProductInfo(pricePolicyId);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, productInfo));

        // when
        GetMyReviewsResult result = getReviewService.getWriterReviews(
                userId, -1L, 4, Sort.Direction.DESC, ReviewSortProperty.ID
        );

        // then
        assertThat(result.reviews()).hasSize(1);
        assertThat(result.reviews().getFirst().product()).isNotNull();
        assertThat(result.reviews().getFirst().product().unitAmount()).isNull();
    }

    @Test
    @DisplayName("나의 리뷰 목록에 unitAmount가 있는 리뷰와 없는 리뷰가 혼재할 때 각각 올바른 값을 반환한다")
    void getWriterReviews_mixedUnitAmount_returnsCorrectValuesPerReview() {
        // given
        Long userId = 10L;
        Long pricePolicyId = 1001L;
        Long unitAmount = 15000L;
        Review reviewWithAmount = buildReviewWithUnitAmount(101L, userId, 20L, pricePolicyId, false, unitAmount);
        Review reviewWithoutAmount = buildReview(102L, userId, 20L, pricePolicyId, false);
        Reviews reviews = Reviews.from(List.of(reviewWithAmount, reviewWithoutAmount));

        when(findReviewPort.findUserReviews(eq(userId), eq(-1L), any(Pageable.class), eq(ReviewSortProperty.ID)))
                .thenReturn(reviews);
        when(findReviewPort.countActive(userId)).thenReturn(2L);

        ProductInfoResult productInfo = buildProductInfo(pricePolicyId);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, productInfo));

        // when
        GetMyReviewsResult result = getReviewService.getWriterReviews(
                userId, -1L, 4, Sort.Direction.DESC, ReviewSortProperty.ID
        );

        // then
        assertThat(result.reviews()).hasSize(2);
        assertThat(result.reviews().get(0).product().unitAmount()).isEqualTo(unitAmount);
        assertThat(result.reviews().get(1).product().unitAmount()).isNull();
    }

    @Test
    @DisplayName("동일 pricePolicyId를 가진 리뷰들이 서로 다른 unitAmount를 가질 때 각 리뷰별 정확한 unitAmount를 반환한다")
    void getWriterReviews_samePricePolicyDifferentUnitAmount_returnsIndividualValues() {
        // given
        Long userId = 10L;
        Long pricePolicyId = 1001L;
        Review review1 = buildReviewWithUnitAmount(101L, userId, 20L, pricePolicyId, false, 15000L);
        Review review2 = buildReviewWithUnitAmount(102L, userId, 20L, pricePolicyId, false, 18000L);
        Reviews reviews = Reviews.from(List.of(review1, review2));

        when(findReviewPort.findUserReviews(eq(userId), eq(-1L), any(Pageable.class), eq(ReviewSortProperty.ID)))
                .thenReturn(reviews);
        when(findReviewPort.countActive(userId)).thenReturn(2L);

        ProductInfoResult productInfo = buildProductInfo(pricePolicyId);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, productInfo));

        // when
        GetMyReviewsResult result = getReviewService.getWriterReviews(
                userId, -1L, 4, Sort.Direction.DESC, ReviewSortProperty.ID
        );

        // then
        assertThat(result.reviews()).hasSize(2);
        assertThat(result.reviews().get(0).product().unitAmount()).isEqualTo(15000L);
        assertThat(result.reviews().get(1).product().unitAmount()).isEqualTo(18000L);
    }

    private Review buildReviewWithUnitAmount(
            Long id, Long reviewerId, Long productId, Long pricePolicyId, boolean isPhoto, Long unitAmount
    ) {
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
                        .maskedReviewerName("사*자-" + id)
                        .rating(5.0f)
                        .content("리뷰-" + id)
                        .isPhoto(isPhoto)
                        .isEdited(false)
                        .likeCount(3)
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .orderNum(id)
                        .unitAmount(unitAmount)
                        .build()
        );
    }

    private ProductInfoResult buildProductInfo(Long pricePolicyId) {
        return new ProductInfoResult(
                1L,
                "테스트 상품",
                "테스트 브랜드",
                new ProductPricePolicyInfoResult(pricePolicyId, 20000L, 15000L, BigDecimal.valueOf(25), 100L),
                List.of(),
                new GetFileResult(1L, "CATALOG", "jpg", "catalog.jpg", "https://example.com/catalog.jpg", List.of(), 1L)
        );
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
                        .maskedReviewerName("사*자-" + id)
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
