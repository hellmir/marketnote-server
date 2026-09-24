package com.personal.marketnote.community.service.review;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.domain.review.Rating;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewSnapshotState;
import com.personal.marketnote.community.exception.ReviewNotFoundException;
import com.personal.marketnote.community.port.in.result.review.GetReviewKeyResult;
import com.personal.marketnote.community.port.out.review.FindReviewPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetReviewKeyServiceTest {
    @Mock
    private FindReviewPort findReviewPort;

    @InjectMocks
    private GetReviewKeyService getReviewKeyService;

    @Test
    @DisplayName("리뷰 작성자 본인이 요청하면 reviewKey를 반환한다")
    void shouldReturnReviewKeyWhenRequesterIsReviewer() {
        Long reviewId = 1L;
        Long reviewerId = 100L;
        UUID reviewKey = UUID.randomUUID();
        Review review = createReview(reviewId, reviewerId, reviewKey);
        when(findReviewPort.findById(reviewId)).thenReturn(Optional.of(review));

        GetReviewKeyResult result = getReviewKeyService.getReviewKey(reviewId, reviewerId);

        assertThat(result.reviewKey()).isEqualTo(reviewKey);
    }

    @Test
    @DisplayName("리뷰 작성자가 아닌 사용자가 요청하면 ReviewNotFoundException이 발생한다")
    void shouldThrowWhenRequesterIsNotReviewer() {
        Long reviewId = 1L;
        Long reviewerId = 100L;
        Long otherUserId = 200L;
        Review review = createReview(reviewId, reviewerId, UUID.randomUUID());
        when(findReviewPort.findById(reviewId)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> getReviewKeyService.getReviewKey(reviewId, otherUserId))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 reviewId로 요청하면 ReviewNotFoundException이 발생한다")
    void shouldThrowWhenReviewNotFound() {
        Long reviewId = 999L;
        Long reviewerId = 100L;
        when(findReviewPort.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getReviewKeyService.getReviewKey(reviewId, reviewerId))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    private Review createReview(Long id, Long reviewerId, UUID reviewKey) {
        return Review.from(ReviewSnapshotState.builder()
                .id(id)
                .reviewKey(reviewKey)
                .reviewerId(reviewerId)
                .orderId(200L)
                .productId(300L)
                .pricePolicyId(400L)
                .reviewerName("홍길동")
                .maskedReviewerName("홍**")
                .rating(Rating.of(5.0f))
                .content("테스트 리뷰 내용")
                .isPhoto(false)
                .isEdited(false)
                .likeCount(0)
                .status(EntityStatus.ACTIVE)
                .build());
    }
}
