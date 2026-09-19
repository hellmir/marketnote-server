package com.personal.marketnote.community.domain.review;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.money.Money;
import com.personal.marketnote.common.utility.ValueMasker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewTest {

    @Test
    @DisplayName("CreateState로 생성하면 reviewerName이 마스킹된다")
    void shouldMaskReviewerNameWhenCreatedFromCreateState() {
        ReviewCreateState state = createDefaultCreateState(5.0f, "홍길동");

        Review review = Review.from(state);

        assertThat(review.getMaskedReviewerName()).isEqualTo(ValueMasker.mask("홍길동"));
        assertThat(review.getMaskedReviewerName()).isNotEqualTo("홍길동");
    }

    @Test
    @DisplayName("CreateState로 생성하면 별점이 HALF_UP으로 반올림된다 (올림: 4.5 → 5.0)")
    void shouldRoundUpRatingWhenFractionIsHalf() {
        ReviewCreateState state = createDefaultCreateState(4.5f);

        Review review = Review.from(state);

        assertThat(review.getRating()).isEqualTo(5.0f);
    }

    @Test
    @DisplayName("CreateState로 생성하면 별점이 HALF_UP으로 반올림된다 (버림: 4.4 → 4.0)")
    void shouldRoundDownRatingWhenFractionIsBelowHalf() {
        ReviewCreateState state = createDefaultCreateState(4.4f);

        Review review = Review.from(state);

        assertThat(review.getRating()).isEqualTo(4.0f);
    }

    @Test
    @DisplayName("CreateState로 생성하면 정수 별점은 변화 없이 유지된다")
    void shouldKeepIntegerRatingUnchanged() {
        ReviewCreateState state = createDefaultCreateState(3.0f);

        Review review = Review.from(state);

        assertThat(review.getRating()).isEqualTo(3.0f);
    }

    @Test
    @DisplayName("CreateState로 생성하면 초기 상태가 ACTIVE로 설정된다")
    void shouldSetInitialStatusToActive() {
        ReviewCreateState state = createDefaultCreateState(5.0f);

        Review review = Review.from(state);

        assertThat(review.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(review.isActive()).isTrue();
    }

    @Test
    @DisplayName("SnapshotState로 복원하면 모든 필드가 올바르게 매핑된다")
    void shouldRestoreAllFieldsFromSnapshotState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2026, 4, 1, 12, 0);
        ReviewSnapshotState state = ReviewSnapshotState.builder()
                .id(1L)
                .reviewerId(100L)
                .orderId(200L)
                .productId(300L)
                .pricePolicyId(400L)
                .productImageUrl("https://example.com/image.jpg")
                .selectedOptions("옵션A / 옵션B")
                .quantity(2)
                .reviewerName("홍길동")
                .maskedReviewerName("홍**")
                .rating(5.0f)
                .content("좋은 상품입니다")
                .isPhoto(true)
                .isEdited(false)
                .likeCount(10)
                .status(EntityStatus.ACTIVE)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .orderNum(1L)
                .unitAmount(15000L)
                .build();

        Review review = Review.from(state);

        assertThat(review.getId()).isEqualTo(1L);
        assertThat(review.getReviewerId()).isEqualTo(100L);
        assertThat(review.getOrderId()).isEqualTo(200L);
        assertThat(review.getProductId()).isEqualTo(300L);
        assertThat(review.getPricePolicyId()).isEqualTo(400L);
        assertThat(review.getProductImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(review.getSelectedOptions()).isEqualTo("옵션A / 옵션B");
        assertThat(review.getQuantity()).isEqualTo(2);
        assertThat(review.getReviewerName()).isEqualTo("홍길동");
        assertThat(review.getMaskedReviewerName()).isEqualTo("홍**");
        assertThat(review.getRating()).isEqualTo(5.0f);
        assertThat(review.getContent()).isEqualTo("좋은 상품입니다");
        assertThat(review.getIsPhoto()).isTrue();
        assertThat(review.getIsEdited()).isFalse();
        assertThat(review.getLikeCount()).isEqualTo(10);
        assertThat(review.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(review.getCreatedAt()).isEqualTo(createdAt);
        assertThat(review.getModifiedAt()).isEqualTo(modifiedAt);
        assertThat(review.getOrderNum()).isEqualTo(1L);
        assertThat(review.getUnitAmount()).isEqualTo(Money.of(15000L));
    }

    @Test
    @DisplayName("별점 3.5는 HALF_UP 반올림으로 4.0이 된다")
    void shouldRoundThreePointFiveToFour() {
        ReviewCreateState state = createDefaultCreateState(3.5f);

        Review review = Review.from(state);

        assertThat(review.getRating()).isEqualTo(4.0f);
    }

    @Test
    @DisplayName("별점 2.5는 HALF_UP 반올림으로 3.0이 된다")
    void shouldRoundTwoPointFiveToThree() {
        ReviewCreateState state = createDefaultCreateState(2.5f);

        Review review = Review.from(state);

        assertThat(review.getRating()).isEqualTo(3.0f);
    }

    @Test
    @DisplayName("별점 1.5는 HALF_UP 반올림으로 2.0이 된다")
    void shouldRoundOnePointFiveToTwo() {
        ReviewCreateState state = createDefaultCreateState(1.5f);

        Review review = Review.from(state);

        assertThat(review.getRating()).isEqualTo(2.0f);
    }

    @Test
    @DisplayName("delete를 호출하면 상태가 INACTIVE로 변경된다")
    void shouldChangeStatusToInactiveWhenDeleted() {
        Review review = createActiveReview();

        review.delete();

        assertThat(review.getStatus()).isEqualTo(EntityStatus.INACTIVE);
        assertThat(review.isInactive()).isTrue();
        assertThat(review.isActive()).isFalse();
    }

    @Test
    @DisplayName("ACTIVE 상태에서 changeExposure를 호출하면 UNEXPOSED로 전환된다")
    void shouldChangeFromActiveToUnexposedOnChangeExposure() {
        Review review = createActiveReview();

        review.changeExposure();

        assertThat(review.getStatus()).isEqualTo(EntityStatus.UNEXPOSED);
    }

    @Test
    @DisplayName("UNEXPOSED 상태에서 changeExposure를 호출하면 ACTIVE로 전환된다")
    void shouldChangeFromUnexposedToActiveOnChangeExposure() {
        Review review = createReviewWithStatus(EntityStatus.UNEXPOSED);

        review.changeExposure();

        assertThat(review.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("INACTIVE 상태에서 changeExposure를 호출하면 ACTIVE로 전환된다")
    void shouldChangeFromInactiveToActiveOnChangeExposure() {
        Review review = createReviewWithStatus(EntityStatus.INACTIVE);

        review.changeExposure();

        assertThat(review.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("isStatusChanged는 현재 활성 상태와 인자가 다르면 true를 반환한다")
    void shouldReturnTrueWhenStatusDiffersFromArgument() {
        Review review = createActiveReview();

        assertThat(review.isStatusChanged(false)).isTrue();
    }

    @Test
    @DisplayName("isStatusChanged는 현재 활성 상태와 인자가 같으면 false를 반환한다")
    void shouldReturnFalseWhenStatusMatchesArgument() {
        Review review = createActiveReview();

        assertThat(review.isStatusChanged(true)).isFalse();
    }

    @Test
    @DisplayName("hasUnitAmount는 unitAmount가 존재하면 true, null이면 false를 반환한다")
    void shouldReturnCorrectHasUnitAmountBasedOnValue() {
        Review reviewWithAmount = createReviewWithUnitAmount(10000L);
        Review reviewWithoutAmount = createReviewWithUnitAmount(null);

        assertThat(reviewWithAmount.hasUnitAmount()).isTrue();
        assertThat(reviewWithoutAmount.hasUnitAmount()).isFalse();
    }

    @Test
    @DisplayName("update를 호출하면 rating, content, isPhoto가 변경된다")
    void shouldUpdateRatingContentAndIsPhoto() {
        Review review = createActiveReview();

        review.update(3.0f, "수정된 리뷰 내용", true);

        assertThat(review.getRating()).isEqualTo(3.0f);
        assertThat(review.getContent()).isEqualTo("수정된 리뷰 내용");
        assertThat(review.getIsPhoto()).isTrue();
        assertThat(review.getIsEdited()).isFalse();
    }

    @Test
    @DisplayName("update에 소수점 별점을 전달하면 반올림 없이 그대로 저장된다")
    void shouldNotRoundRatingOnUpdate() {
        Review review = createActiveReview();

        review.update(3.7f, "수정된 리뷰 내용", true);

        assertThat(review.getRating()).isEqualTo(3.7f);
    }

    @Test
    @DisplayName("updateIsUserLiked에 true를 전달하면 isUserLiked가 true가 된다")
    void shouldSetIsUserLikedToTrue() {
        Review review = createActiveReview();

        review.updateIsUserLiked(true);

        assertThat(review.isUserLiked()).isTrue();
    }

    @Test
    @DisplayName("updateIsUserLiked에 false를 전달하면 isUserLiked가 false가 된다")
    void shouldSetIsUserLikedToFalse() {
        Review review = createActiveReview();
        review.updateIsUserLiked(true);

        review.updateIsUserLiked(false);

        assertThat(review.isUserLiked()).isFalse();
    }

    private ReviewCreateState createDefaultCreateState(Float rating) {
        return createDefaultCreateState(rating, "홍길동");
    }

    private ReviewCreateState createDefaultCreateState(Float rating, String reviewerName) {
        return ReviewCreateState.builder()
                .reviewerId(100L)
                .orderId(200L)
                .productId(300L)
                .pricePolicyId(400L)
                .productImageUrl("https://example.com/image.jpg")
                .selectedOptions("옵션A")
                .quantity(1)
                .reviewerName(reviewerName)
                .rating(rating)
                .content("테스트 리뷰 내용")
                .isPhoto(false)
                .unitAmount(15000L)
                .build();
    }

    private Review createActiveReview() {
        return createReviewWithStatus(EntityStatus.ACTIVE);
    }

    private Review createReviewWithStatus(EntityStatus status) {
        return Review.from(ReviewSnapshotState.builder()
                .id(1L)
                .reviewerId(100L)
                .orderId(200L)
                .productId(300L)
                .pricePolicyId(400L)
                .reviewerName("홍길동")
                .maskedReviewerName("홍**")
                .rating(5.0f)
                .content("테스트 리뷰 내용")
                .isPhoto(false)
                .isEdited(false)
                .likeCount(0)
                .status(status)
                .unitAmount(15000L)
                .build());
    }

    private Review createReviewWithUnitAmount(Long unitAmount) {
        return Review.from(ReviewSnapshotState.builder()
                .id(1L)
                .reviewerId(100L)
                .orderId(200L)
                .productId(300L)
                .pricePolicyId(400L)
                .reviewerName("홍길동")
                .maskedReviewerName("홍**")
                .rating(5.0f)
                .content("테스트 리뷰 내용")
                .isPhoto(false)
                .isEdited(false)
                .likeCount(0)
                .status(EntityStatus.ACTIVE)
                .unitAmount(unitAmount)
                .build());
    }
}
