package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.community.domain.like.LikeTargetType;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewSnapshotState;
import com.personal.marketnote.community.domain.review.ReviewSortProperty;
import com.personal.marketnote.community.domain.review.Reviews;
import com.personal.marketnote.community.port.in.result.review.GetReviewsResult;
import com.personal.marketnote.community.port.in.result.review.ReviewItemResult;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.personal.marketnote.community.exception.ReviewNotFoundException;
import com.personal.marketnote.community.port.in.result.review.ReviewProductInfoResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetReviewUseCaseTest {
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
    @DisplayName("상품 리뷰 목록 첫 페이지 조회 시 총 개수/다음 Cursor/이미지/좋아요 정보를 포함한다")
    void getProductReviews_firstPage_includesTotalNextCursorImagesAndUserLiked() {
        Long userId = 10L;
        Long productId = 20L;
        int pageSize = 2;
        Review review1 = buildReview(101L, userId, productId, 1001L, true);
        Review review2 = buildReview(102L, 99L, productId, 1002L, false);
        Review review3 = buildReview(103L, 98L, productId, 1003L, true);
        Reviews reviews = Reviews.from(List.of(review1, review2, review3));

        when(findReviewPort.findProductReviews(
                eq(productId),
                eq(false),
                eq(-1L),
                any(Pageable.class),
                eq(ReviewSortProperty.ID)
        )).thenReturn(reviews);
        when(findReviewPort.countActive(productId, false)).thenReturn(3L);

        GetFilesResult imagesResult = buildFilesResult(11L, "review-1.jpg");
        when(findReviewImagesPort.findImagesByReviewIdAndSort(review1.getId(), FileSort.REVIEW_IMAGE))
                .thenReturn(Optional.of(imagesResult));
        when(getLikeUseCase.existsUserLike(LikeTargetType.REVIEW, review1.getId(), userId)).thenReturn(true);
        when(getLikeUseCase.existsUserLike(LikeTargetType.REVIEW, review2.getId(), userId)).thenReturn(false);

        GetReviewsResult result = getReviewService.getProductReviews(
                userId,
                productId,
                false,
                -1L,
                pageSize,
                Sort.Direction.DESC,
                ReviewSortProperty.ID
        );

        assertThat(result.totalElements()).isEqualTo(3L);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(review2.getId());
        assertThat(result.reviews()).hasSize(2);

        ReviewItemResult first = result.reviews().getFirst();
        ReviewItemResult second = result.reviews().get(1);
        assertThat(first.images()).containsExactlyElementsOf(imagesResult.images());
        assertThat(second.images()).isNull();
        assertThat(first.isUserLiked()).isTrue();
        assertThat(second.isUserLiked()).isFalse();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(findReviewPort).findProductReviews(
                eq(productId),
                eq(false),
                eq(-1L),
                pageableCaptor.capture(),
                eq(ReviewSortProperty.ID)
        );
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageSize()).isEqualTo(pageSize + 1);
        String sortProperty = Objects.requireNonNull(ReviewSortProperty.ID.getCamelCaseValue());
        Sort.Order order = pageable.getSort().getOrderFor(sortProperty);
        assertThat(order).isNotNull();
        assertThat(Objects.requireNonNull(order).getDirection()).isEqualTo(Sort.Direction.DESC);

        verify(findReviewPort).countActive(productId, false);
        verify(findReviewImagesPort).findImagesByReviewIdAndSort(review1.getId(), FileSort.REVIEW_IMAGE);
        verify(findReviewImagesPort, never())
                .findImagesByReviewIdAndSort(review2.getId(), FileSort.REVIEW_IMAGE);
        verify(findReviewImagesPort, never())
                .findImagesByReviewIdAndSort(review3.getId(), FileSort.REVIEW_IMAGE);
        verify(getLikeUseCase).existsUserLike(LikeTargetType.REVIEW, review1.getId(), userId);
        verify(getLikeUseCase).existsUserLike(LikeTargetType.REVIEW, review2.getId(), userId);
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("로그인 사용자가 좋아요를 누른 경우 true, 누르지 않은 경우 false를 반환한다")
    void getProductReviews_userLikedFlag_matchesLikeExists() {
        Long userId = 77L;
        Long productId = 88L;
        Long cursor = 999L;
        int pageSize = 2;
        Review review1 = buildReview(401L, 1L, productId, 4001L, false);
        Review review2 = buildReview(402L, 2L, productId, 4002L, false);
        Reviews reviews = Reviews.from(List.of(review1, review2));

        when(findReviewPort.findProductReviews(
                eq(productId),
                eq(false),
                eq(cursor),
                any(Pageable.class),
                eq(ReviewSortProperty.ID)
        )).thenReturn(reviews);
        when(getLikeUseCase.existsUserLike(LikeTargetType.REVIEW, review1.getId(), userId)).thenReturn(true);
        when(getLikeUseCase.existsUserLike(LikeTargetType.REVIEW, review2.getId(), userId)).thenReturn(false);

        GetReviewsResult result = getReviewService.getProductReviews(
                userId,
                productId,
                false,
                cursor,
                pageSize,
                Sort.Direction.DESC,
                ReviewSortProperty.ID
        );

        assertThat(result.reviews()).hasSize(2);
        assertThat(result.reviews().getFirst().isUserLiked()).isTrue();
        assertThat(result.reviews().get(1).isUserLiked()).isFalse();

        verify(findReviewPort, never()).countActive(productId, false);
        verify(getLikeUseCase).existsUserLike(LikeTargetType.REVIEW, review1.getId(), userId);
        verify(getLikeUseCase).existsUserLike(LikeTargetType.REVIEW, review2.getId(), userId);
        verifyNoInteractions(findReviewImagesPort, findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 시 첫 페이지가 아니면(Cursor 값 존재) 총 개수 조회를 생략한다")
    void getProductReviews_withCursor_skipsTotalElements() {
        Long productId = 30L;
        Long cursor = 200L;
        int pageSize = 2;
        Review review1 = buildReview(201L, 1L, productId, 2001L, false);
        Review review2 = buildReview(202L, 2L, productId, 2002L, false);
        Reviews reviews = Reviews.from(List.of(review1, review2));

        when(findReviewPort.findProductReviews(
                eq(productId),
                eq(false),
                eq(cursor),
                any(Pageable.class),
                eq(ReviewSortProperty.ORDER_NUM)
        )).thenReturn(reviews);

        GetReviewsResult result = getReviewService.getProductReviews(
                null,
                productId,
                false,
                cursor,
                pageSize,
                Sort.Direction.ASC,
                ReviewSortProperty.ORDER_NUM
        );

        assertThat(result.totalElements()).isNull();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isEqualTo(review2.getId());
        assertThat(result.reviews()).hasSize(2);
        assertThat(result.reviews().getFirst().isUserLiked()).isFalse();

        verify(findReviewPort, never()).countActive(productId, false);
        verifyNoInteractions(findReviewImagesPort, findProductByPricePolicyPort, getLikeUseCase);
    }

    @Test
    @DisplayName("상품 리뷰 목록이 비어 있으면 빈 결과를 반환한다")
    void getProductReviews_empty_returnsEmptyResult() {
        Long productId = 40L;
        int pageSize = 3;
        Reviews reviews = Reviews.from(List.of());

        when(findReviewPort.findProductReviews(
                eq(productId),
                eq(false),
                eq(-1L),
                any(Pageable.class),
                eq(ReviewSortProperty.ID)
        )).thenReturn(reviews);
        when(findReviewPort.countActive(productId, false)).thenReturn(0L);

        GetReviewsResult result = getReviewService.getProductReviews(
                1L,
                productId,
                false,
                -1L,
                pageSize,
                Sort.Direction.DESC,
                ReviewSortProperty.ID
        );

        assertThat(result.totalElements()).isEqualTo(0L);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.reviews()).isEmpty();

        verify(findReviewPort).countActive(productId, false);
        verifyNoInteractions(findReviewImagesPort, findProductByPricePolicyPort, getLikeUseCase);
    }

    @Test
    @DisplayName("포토 리뷰 요청인 경우 로그인 사용자 좋아요 여부를 갱신하지 않는다")
    void getProductReviews_photoOnly_skipsUserLikedUpdate() {
        Long userId = 50L;
        Long productId = 60L;
        int pageSize = 2;
        Review review = buildReview(301L, userId, productId, 3001L, true);
        Reviews reviews = Reviews.from(List.of(review));

        when(findReviewPort.findProductReviews(
                eq(productId),
                eq(true),
                eq(-1L),
                any(Pageable.class),
                eq(ReviewSortProperty.ID)
        )).thenReturn(reviews);
        when(findReviewPort.countActive(productId, true)).thenReturn(1L);

        GetFilesResult imagesResult = buildFilesResult(21L, "review-photo.jpg");
        when(findReviewImagesPort.findImagesByReviewIdAndSort(review.getId(), FileSort.REVIEW_IMAGE))
                .thenReturn(Optional.of(imagesResult));

        GetReviewsResult result = getReviewService.getProductReviews(
                userId,
                productId,
                true,
                -1L,
                pageSize,
                Sort.Direction.DESC,
                ReviewSortProperty.ID
        );

        ReviewItemResult item = result.reviews().getFirst();
        assertThat(item.isUserLiked()).isFalse();
        assertThat(item.images()).containsExactlyElementsOf(imagesResult.images());
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.nextCursor()).isEqualTo(review.getId());
        assertThat(result.hasNext()).isFalse();

        verify(findReviewPort).countActive(productId, true);
        verifyNoInteractions(findProductByPricePolicyPort, getLikeUseCase);
    }

    // ========== 응답 분리 ==========

    @Test
    @DisplayName("상품 리뷰 목록 조회 시 응답에 maskedReviewerName이 포함된다")
    void getProductReviews_containsMaskedReviewerName() {
        // given
        Long userId = 10L;
        Long productId = 20L;
        int pageSize = 2;
        Review review = buildReview(101L, userId, productId, 1001L, false);
        Reviews reviews = Reviews.from(List.of(review));

        when(findReviewPort.findProductReviews(
                eq(productId),
                eq(false),
                eq(-1L),
                any(Pageable.class),
                eq(ReviewSortProperty.ID)
        )).thenReturn(reviews);
        when(findReviewPort.countActive(productId, false)).thenReturn(1L);

        // when
        GetReviewsResult result = getReviewService.getProductReviews(
                userId,
                productId,
                false,
                -1L,
                pageSize,
                Sort.Direction.DESC,
                ReviewSortProperty.ID
        );

        // then
        assertThat(result.reviews()).hasSize(1);
        assertThat(result.reviews().getFirst().maskedReviewerName()).isEqualTo("사*자-101");
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 시 응답에 reviewerName이 포함되지 않는다")
    void getProductReviews_doesNotContainReviewerName() {
        // given
        Long userId = 10L;
        Long productId = 20L;
        int pageSize = 2;
        Review review = buildReview(101L, userId, productId, 1001L, false);
        Reviews reviews = Reviews.from(List.of(review));

        when(findReviewPort.findProductReviews(
                eq(productId),
                eq(false),
                eq(-1L),
                any(Pageable.class),
                eq(ReviewSortProperty.ID)
        )).thenReturn(reviews);
        when(findReviewPort.countActive(productId, false)).thenReturn(1L);

        // when
        GetReviewsResult result = getReviewService.getProductReviews(
                userId,
                productId,
                false,
                -1L,
                pageSize,
                Sort.Direction.DESC,
                ReviewSortProperty.ID
        );

        // then
        assertThat(result.reviews()).hasSize(1);
        // ReviewItemResult에는 reviewerName 필드가 존재하지 않음을 컴파일 타임에 보장
        // maskedReviewerName만 포함되어 있는지 검증
        assertThat(result.reviews().getFirst().maskedReviewerName()).isNotNull();
        assertThat(result.reviews().getFirst()).hasNoNullFieldsOrPropertiesExcept(
                "images", "product"
        );
    }

    // ========== getReviewDetail ==========

    @Test
    @DisplayName("리뷰 상세 조회 시 주문 상품 정보(상품명, 브랜드, 구매 시점 가격, 선택 옵션, 대표 이미지)가 응답에 포함된다")
    void getReviewDetail_includesOrderProductInfo() {
        // given
        Long reviewId = 101L;
        Long userId = 10L;
        Long pricePolicyId = 1001L;
        Long unitAmount = 15000L;
        Review review = buildReview(reviewId, userId, 20L, pricePolicyId, true);

        when(findReviewPort.findById(reviewId)).thenReturn(Optional.of(review));
        when(getLikeUseCase.existsUserLike(LikeTargetType.REVIEW, reviewId, userId)).thenReturn(true);

        GetFilesResult imagesResult = buildFilesResult(11L, "review-detail.jpg");
        when(findReviewImagesPort.findImagesByReviewIdAndSort(reviewId, FileSort.REVIEW_IMAGE))
                .thenReturn(Optional.of(imagesResult));

        ProductInfoResult productInfo = new ProductInfoResult(
                1L,
                "테스트 상품",
                "테스트 브랜드",
                new ProductPricePolicyInfoResult(pricePolicyId, 20000L, 15000L, BigDecimal.valueOf(25), 100L),
                List.of(),
                new GetFileResult(1L, "CATALOG", "jpg", "catalog.jpg", "https://example.com/catalog.jpg", List.of(), 1L)
        );
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of(pricePolicyId, productInfo));
        when(findOrderProductPort.findUnitAmountByOrderIdAndPricePolicyId(review.getOrderId(), pricePolicyId))
                .thenReturn(Optional.of(unitAmount));

        // when
        ReviewItemResult result = getReviewService.getReviewDetail(reviewId, userId);

        // then
        assertThat(result.id()).isEqualTo(reviewId);
        assertThat(result.isUserLiked()).isTrue();
        assertThat(result.images()).containsExactlyElementsOf(imagesResult.images());

        ReviewProductInfoResult product = result.product();
        assertThat(product).isNotNull();
        assertThat(product.name()).isEqualTo("테스트 상품");
        assertThat(product.brandName()).isEqualTo("테스트 브랜드");
        assertThat(product.unitAmount()).isEqualTo(unitAmount);
        assertThat(product.catalogImage()).isNotNull();

        verify(findReviewPort).findById(reviewId);
        verify(getLikeUseCase).existsUserLike(LikeTargetType.REVIEW, reviewId, userId);
        verify(findReviewImagesPort).findImagesByReviewIdAndSort(reviewId, FileSort.REVIEW_IMAGE);
        verify(findProductByPricePolicyPort).findByPricePolicyIds(List.of(pricePolicyId));
        verify(findOrderProductPort).findUnitAmountByOrderIdAndPricePolicyId(review.getOrderId(), pricePolicyId);
    }

    @Test
    @DisplayName("주문 상품 정보를 조회할 수 없는 경우 product가 null로 반환된다")
    void getReviewDetail_productNotFound_returnsNullProduct() {
        // given
        Long reviewId = 102L;
        Long userId = 10L;
        Long pricePolicyId = 2001L;
        Review review = buildReview(reviewId, userId, 20L, pricePolicyId, false);

        when(findReviewPort.findById(reviewId)).thenReturn(Optional.of(review));
        when(getLikeUseCase.existsUserLike(LikeTargetType.REVIEW, reviewId, userId)).thenReturn(false);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of());

        // when
        ReviewItemResult result = getReviewService.getReviewDetail(reviewId, userId);

        // then
        assertThat(result.id()).isEqualTo(reviewId);
        assertThat(result.product()).isNull();
        assertThat(result.isUserLiked()).isFalse();

        verify(findReviewPort).findById(reviewId);
        verify(getLikeUseCase).existsUserLike(LikeTargetType.REVIEW, reviewId, userId);
        verifyNoInteractions(findOrderProductPort);
    }

    @Test
    @DisplayName("리뷰 상세 조회 시 사용자 좋아요 여부가 포함된다")
    void getReviewDetail_includesUserLikedStatus() {
        // given
        Long reviewId = 103L;
        Long userId = 10L;
        Long pricePolicyId = 3001L;
        Review review = buildReview(reviewId, userId, 20L, pricePolicyId, false);

        when(findReviewPort.findById(reviewId)).thenReturn(Optional.of(review));
        when(getLikeUseCase.existsUserLike(LikeTargetType.REVIEW, reviewId, userId)).thenReturn(true);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId)))
                .thenReturn(Map.of());

        // when
        ReviewItemResult result = getReviewService.getReviewDetail(reviewId, userId);

        // then
        assertThat(result.isUserLiked()).isTrue();

        verify(getLikeUseCase).existsUserLike(LikeTargetType.REVIEW, reviewId, userId);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 조회 시 ReviewNotFoundException이 발생한다")
    void getReviewDetail_notFound_throwsReviewNotFoundException() {
        // given
        Long reviewId = 999L;
        Long userId = 10L;

        when(findReviewPort.findById(reviewId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getReviewService.getReviewDetail(reviewId, userId))
                .isInstanceOf(ReviewNotFoundException.class);

        verify(findReviewPort).findById(reviewId);
        verifyNoInteractions(getLikeUseCase, findReviewImagesPort, findProductByPricePolicyPort, findOrderProductPort);
    }

    private Review buildReview(
            Long id,
            Long reviewerId,
            Long productId,
            Long pricePolicyId,
            boolean isPhoto
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
                        .build()
        );
    }

    private GetFilesResult buildFilesResult(Long id, String name) {
        return new GetFilesResult(
                List.of(
                        new GetFileResult(
                                id,
                                FileSort.REVIEW_IMAGE.name(),
                                "jpg",
                                name,
                                "https://example.com/" + name,
                                List.of(),
                                1L
                        )
                )
        );
    }
}
