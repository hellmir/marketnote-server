package com.personal.marketnote.community.domain.review;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.money.Money;
import com.personal.marketnote.common.utility.ValueMasker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewTest {

    @Test
    @DisplayName("CreateState로 생성하면 reviewerName이 마스킹된다")
    void shouldMaskReviewerNameWhenCreatedFromCreateState() {
        ReviewCreateState state = createDefaultCreateState(Rating.of(5.0f), "홍길동");

        Review review = Review.from(state);

        assertThat(review.getMaskedReviewerName()).isEqualTo(ValueMasker.mask("홍길동"));
        assertThat(review.getMaskedReviewerName()).isNotEqualTo("홍길동");
    }

    @Test
    @DisplayName("CreateState로 생성하면 정수 별점은 그대로 유지된다")
    void shouldKeepIntegerRatingUnchanged() {
        ReviewCreateState state = createDefaultCreateState(Rating.of(3.0f));

        Review review = Review.from(state);

        assertThat(review.getRating().getValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("CreateState로 생성하면 초기 상태가 ACTIVE로 설정된다")
    void shouldSetInitialStatusToActive() {
        ReviewCreateState state = createDefaultCreateState(Rating.of(5.0f));

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
                .rating(Rating.of(5.0f))
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
        assertThat(review.getQuantity().getValue()).isEqualTo(2);
        assertThat(review.getReviewerName()).isEqualTo("홍길동");
        assertThat(review.getMaskedReviewerName()).isEqualTo("홍**");
        assertThat(review.getRating().getValue()).isEqualTo(5);
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

        review.update(Rating.of(3.0f), "수정된 리뷰 내용", true);

        assertThat(review.getRating().getValue()).isEqualTo(3);
        assertThat(review.getContent()).isEqualTo("수정된 리뷰 내용");
        assertThat(review.getIsPhoto()).isTrue();
        assertThat(review.getIsEdited()).isFalse();
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

    @Test
    @DisplayName("CreateState로 생성하면 reviewKey가 자동으로 생성된다")
    void shouldGenerateReviewKeyWhenCreatedFromCreateState() {
        ReviewCreateState state = createDefaultCreateState(Rating.of(5.0f));

        Review review = Review.from(state);

        assertThat(review.getReviewKey()).isNotNull();
    }

    @Test
    @DisplayName("CreateState로 두 번 생성하면 각각 다른 reviewKey가 생성된다")
    void shouldGenerateUniqueReviewKeyOnEachCreation() {
        ReviewCreateState state = createDefaultCreateState(Rating.of(5.0f));

        Review first = Review.from(state);
        Review second = Review.from(state);

        assertThat(first.getReviewKey()).isNotEqualTo(second.getReviewKey());
    }

    @Test
    @DisplayName("SnapshotState로 복원하면 reviewKey가 state에서 복원된다")
    void shouldRestoreReviewKeyFromSnapshotState() {
        UUID reviewKey = UUID.randomUUID();
        ReviewSnapshotState state = ReviewSnapshotState.builder()
                .id(1L)
                .reviewerId(100L)
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
                .unitAmount(15000L)
                .reviewKey(reviewKey)
                .build();

        Review review = Review.from(state);

        assertThat(review.getReviewKey()).isEqualTo(reviewKey);
    }

    private ReviewCreateState createDefaultCreateState(Rating rating) {
        return createDefaultCreateState(rating, "홍길동");
    }

    private ReviewCreateState createDefaultCreateState(Rating rating, String reviewerName) {
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
                .rating(Rating.of(5.0f))
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
                .rating(Rating.of(5.0f))
                .content("테스트 리뷰 내용")
                .isPhoto(false)
                .isEdited(false)
                .likeCount(0)
                .status(EntityStatus.ACTIVE)
                .unitAmount(unitAmount)
                .build());
    }
}
