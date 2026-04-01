package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewSnapshotState;
import com.personal.marketnote.community.domain.review.ReviewSortProperty;
import com.personal.marketnote.community.domain.review.Reviews;
import com.personal.marketnote.community.port.in.command.review.GetUserReviewsCommand;
import com.personal.marketnote.community.port.in.result.review.GetUserReviewsResult;
import com.personal.marketnote.community.port.out.file.FindReviewImagesPort;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.community.port.out.review.FindReviewPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.personal.marketnote.common.domain.file.FileSort.REVIEW_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserReviewsUseCaseTest {
    @Mock
    private FindReviewPort findReviewPort;
    @Mock
    private FindReviewImagesPort findReviewImagesPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;

    @InjectMocks
    private GetUserReviewsService getUserReviewsService;

    // ========== A. 기본 조회 ==========

    @Test
    @DisplayName("회원의 리뷰 내역을 오프셋 기반으로 조회한다")
    void getUserReviews_returnsOffsetBasedResult() {
        // given
        Long userId = 1L;
        GetUserReviewsCommand command = new GetUserReviewsCommand(
                userId, 1, 10, Sort.Direction.DESC, ReviewSortProperty.ID
        );
        Reviews reviews = Reviews.from(List.of(
                buildReview(1L, userId),
                buildReview(2L, userId)
        ));
        mockFindUserReviewsByOffset(reviews);
        when(findReviewPort.countActive(userId)).thenReturn(2L);

        // when
        GetUserReviewsResult result = getUserReviewsService.getUserReviews(command);

        // then
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(2L);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.reviews()).hasSize(2);
    }

    @Test
    @DisplayName("조회 결과가 비어 있으면 빈 목록과 총 페이지 0을 반환한다")
    void getUserReviews_emptyResult_returnsEmptyListAndZeroPages() {
        // given
        GetUserReviewsCommand command = new GetUserReviewsCommand(
                1L, 1, 10, Sort.Direction.DESC, ReviewSortProperty.ID
        );
        mockFindUserReviewsByOffset(emptyReviews());
        when(findReviewPort.countActive(1L)).thenReturn(0L);

        // when
        GetUserReviewsResult result = getUserReviewsService.getUserReviews(command);

        // then
        assertThat(result.reviews()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    // ========== B. 페이지네이션 ==========

    @Test
    @DisplayName("totalPages가 올바르게 계산된다 (올림)")
    void getUserReviews_totalPagesComputedCorrectly() {
        // given
        GetUserReviewsCommand command = new GetUserReviewsCommand(
                1L, 1, 3, Sort.Direction.DESC, ReviewSortProperty.ID
        );
        Reviews reviews = Reviews.from(List.of(
                buildReview(1L, 1L),
                buildReview(2L, 1L),
                buildReview(3L, 1L)
        ));
        mockFindUserReviewsByOffset(reviews);
        when(findReviewPort.countActive(1L)).thenReturn(7L);

        // when
        GetUserReviewsResult result = getUserReviewsService.getUserReviews(command);

        // then
        assertThat(result.totalPages()).isEqualTo(3); // ceil(7/3) = 3
    }

    @Test
    @DisplayName("page와 pageSize가 결과에 그대로 반영된다")
    void getUserReviews_pageAndPageSizeReflected() {
        // given
        GetUserReviewsCommand command = new GetUserReviewsCommand(
                1L, 2, 5, Sort.Direction.ASC, ReviewSortProperty.ID
        );
        mockFindUserReviewsByOffset(emptyReviews());
        when(findReviewPort.countActive(1L)).thenReturn(10L);

        // when
        GetUserReviewsResult result = getUserReviewsService.getUserReviews(command);

        // then
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.pageSize()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(2); // ceil(10/5) = 2
    }

    // ========== C. Port 호출 검증 ==========

    @Test
    @DisplayName("findUserReviewsByOffset이 올바른 파라미터로 호출된다")
    void getUserReviews_callsFindUserReviewsByOffset() {
        // given
        Long userId = 1L;
        GetUserReviewsCommand command = new GetUserReviewsCommand(
                userId, 1, 10, Sort.Direction.DESC, ReviewSortProperty.ID
        );
        mockFindUserReviewsByOffset(emptyReviews());
        when(findReviewPort.countActive(userId)).thenReturn(0L);

        // when
        getUserReviewsService.getUserReviews(command);

        // then
        verify(findReviewPort).findUserReviewsByOffset(
                userId, 1, 10, true, ReviewSortProperty.ID
        );
    }

    @Test
    @DisplayName("countActive가 userId로 호출된다")
    void getUserReviews_callsCountActive() {
        // given
        Long userId = 1L;
        GetUserReviewsCommand command = new GetUserReviewsCommand(
                userId, 1, 10, Sort.Direction.DESC, ReviewSortProperty.ID
        );
        mockFindUserReviewsByOffset(emptyReviews());
        when(findReviewPort.countActive(userId)).thenReturn(0L);

        // when
        getUserReviewsService.getUserReviews(command);

        // then
        verify(findReviewPort).countActive(userId);
    }

    // ========== D. 이미지 ==========

    @Test
    @DisplayName("사진 리뷰이면 이미지가 포함된다")
    void getUserReviews_photoReview_includesImages() {
        // given
        Long userId = 1L;
        GetUserReviewsCommand command = new GetUserReviewsCommand(
                userId, 1, 10, Sort.Direction.DESC, ReviewSortProperty.ID
        );
        Review photoReview = buildPhotoReview(1L, userId);
        Reviews reviews = Reviews.from(List.of(photoReview));
        mockFindUserReviewsByOffset(reviews);
        when(findReviewPort.countActive(userId)).thenReturn(1L);
        GetFileResult fileResult = new GetFileResult(
                1L, "REVIEW_IMAGE", "jpg", "image.jpg", "https://s3/image.jpg", List.of(), 1L
        );
        when(findReviewImagesPort.findImagesByReviewIdAndSort(1L, REVIEW_IMAGE))
                .thenReturn(Optional.of(new GetFilesResult(List.of(fileResult))));

        // when
        GetUserReviewsResult result = getUserReviewsService.getUserReviews(command);

        // then
        assertThat(result.reviews()).hasSize(1);
        assertThat(result.reviews().getFirst().images()).hasSize(1);
    }

    @Test
    @DisplayName("사진 리뷰가 아니면 FindReviewImagesPort가 호출되지 않는다")
    void getUserReviews_nonPhotoReview_doesNotCallImagePort() {
        // given
        Long userId = 1L;
        GetUserReviewsCommand command = new GetUserReviewsCommand(
                userId, 1, 10, Sort.Direction.DESC, ReviewSortProperty.ID
        );
        Reviews reviews = Reviews.from(List.of(buildReview(1L, userId)));
        mockFindUserReviewsByOffset(reviews);
        when(findReviewPort.countActive(userId)).thenReturn(1L);

        // when
        getUserReviewsService.getUserReviews(command);

        // then
        verifyNoInteractions(findReviewImagesPort);
    }

    // ========== E. 상품 정보 ==========

    @Test
    @DisplayName("pricePolicyId가 있는 리뷰이면 상품 정보가 포함된다")
    void getUserReviews_withPricePolicyId_includesProductInfo() {
        // given
        Long userId = 1L;
        GetUserReviewsCommand command = new GetUserReviewsCommand(
                userId, 1, 10, Sort.Direction.DESC, ReviewSortProperty.ID
        );
        Review review = buildReviewWithPricePolicy(1L, userId, 30L);
        Reviews reviews = Reviews.from(List.of(review));
        mockFindUserReviewsByOffset(reviews);
        when(findReviewPort.countActive(userId)).thenReturn(1L);
        ProductInfoResult productInfoResult = new ProductInfoResult(1L, "테스트 상품", "테스트 브랜드", null, List.of(), null);
        when(findProductByPricePolicyPort.findByPricePolicyIds(List.of(30L)))
                .thenReturn(Map.of(30L, productInfoResult));

        // when
        GetUserReviewsResult result = getUserReviewsService.getUserReviews(command);

        // then
        assertThat(result.reviews()).hasSize(1);
        assertThat(result.reviews().getFirst().product()).isNotNull();
    }

    @Test
    @DisplayName("pricePolicyId가 없는 리뷰이면 FindProductByPricePolicyPort가 호출되지 않는다")
    void getUserReviews_withoutPricePolicyId_doesNotCallProductPort() {
        // given
        Long userId = 1L;
        GetUserReviewsCommand command = new GetUserReviewsCommand(
                userId, 1, 10, Sort.Direction.DESC, ReviewSortProperty.ID
        );
        Reviews reviews = Reviews.from(List.of(buildReview(1L, userId)));
        mockFindUserReviewsByOffset(reviews);
        when(findReviewPort.countActive(userId)).thenReturn(1L);

        // when
        getUserReviewsService.getUserReviews(command);

        // then
        verifyNoInteractions(findProductByPricePolicyPort);
    }

    // ========== F. 정렬 ==========

    @Test
    @DisplayName("ASC 정렬 시 isDesc가 false로 전달된다")
    void getUserReviews_ascDirection_isDescFalse() {
        // given
        Long userId = 1L;
        GetUserReviewsCommand command = new GetUserReviewsCommand(
                userId, 1, 10, Sort.Direction.ASC, ReviewSortProperty.ID
        );
        mockFindUserReviewsByOffset(emptyReviews());
        when(findReviewPort.countActive(userId)).thenReturn(0L);

        // when
        getUserReviewsService.getUserReviews(command);

        // then
        verify(findReviewPort).findUserReviewsByOffset(
                userId, 1, 10, false, ReviewSortProperty.ID
        );
    }

    // ========== Review Builders ==========

    private Review buildReview(Long id, Long reviewerId) {
        return Review.from(ReviewSnapshotState.builder()
                .id(id)
                .reviewerId(reviewerId)
                .orderId(100L)
                .productId(50L)
                .rating(5.0f)
                .content("좋은 상품입니다")
                .isPhoto(false)
                .isEdited(false)
                .likeCount(0)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Review buildPhotoReview(Long id, Long reviewerId) {
        return Review.from(ReviewSnapshotState.builder()
                .id(id)
                .reviewerId(reviewerId)
                .orderId(100L)
                .productId(50L)
                .rating(5.0f)
                .content("사진 리뷰입니다")
                .isPhoto(true)
                .isEdited(false)
                .likeCount(0)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Review buildReviewWithPricePolicy(Long id, Long reviewerId, Long pricePolicyId) {
        return Review.from(ReviewSnapshotState.builder()
                .id(id)
                .reviewerId(reviewerId)
                .orderId(100L)
                .productId(50L)
                .pricePolicyId(pricePolicyId)
                .rating(5.0f)
                .content("상품 정보 있는 리뷰")
                .isPhoto(false)
                .isEdited(false)
                .likeCount(0)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    // ========== Mock Helpers ==========

    private Reviews emptyReviews() {
        return Reviews.from(List.of());
    }

    private void mockFindUserReviewsByOffset(Reviews reviews) {
        when(findReviewPort.findUserReviewsByOffset(
                anyLong(), anyInt(), anyInt(), anyBoolean(), any(ReviewSortProperty.class)
        )).thenReturn(reviews);
    }
}
