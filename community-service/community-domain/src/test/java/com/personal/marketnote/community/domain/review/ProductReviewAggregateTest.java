package com.personal.marketnote.community.domain.review;

import com.personal.marketnote.community.domain.review.exception.InvalidRatingPointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class ProductReviewAggregateTest {

    @Test
    @DisplayName("SnapshotState로 복원하면 모든 필드가 올바르게 매핑된다")
    void shouldRestoreAllFieldsFromSnapshotState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2026, 4, 1, 12, 0);
        ProductReviewAggregateSnapshotState state = ProductReviewAggregateSnapshotState.builder()
                .productId(1L)
                .totalCount(10)
                .fivePointCount(5)
                .fourPointCount(2)
                .threePointCount(1)
                .twoPointCount(1)
                .onePointCount(1)
                .totalRating(40.0f)
                .averageRating(4.0f)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();

        ProductReviewAggregate aggregate = ProductReviewAggregate.from(state);

        assertThat(aggregate.getProductId()).isEqualTo(1L);
        assertThat(aggregate.getTotalCount()).isEqualTo(10);
        assertThat(aggregate.getFivePointCount()).isEqualTo(5);
        assertThat(aggregate.getFourPointCount()).isEqualTo(2);
        assertThat(aggregate.getThreePointCount()).isEqualTo(1);
        assertThat(aggregate.getTwoPointCount()).isEqualTo(1);
        assertThat(aggregate.getOnePointCount()).isEqualTo(1);
        assertThat(aggregate.getTotalRating()).isEqualTo(40.0f);
        assertThat(aggregate.getAverageRating()).isEqualTo(4.0f);
        assertThat(aggregate.getCreatedAt()).isEqualTo(createdAt);
        assertThat(aggregate.getModifiedAt()).isEqualTo(modifiedAt);
    }

    @Test
    @DisplayName("Review로 생성하면 해당 별점 카운트가 1 증가하고 평균이 계산된다")
    void shouldCreateFromReviewWithCorrectCountAndRating() {
        Review review = createReviewWithRating(5.0f, 1L);

        ProductReviewAggregate aggregate = ProductReviewAggregate.from(review);

        assertThat(aggregate.getProductId()).isEqualTo(1L);
        assertThat(aggregate.getTotalCount()).isEqualTo(1);
        assertThat(aggregate.getFivePointCount()).isEqualTo(1);
        assertThat(aggregate.getFourPointCount()).isEqualTo(0);
        assertThat(aggregate.getThreePointCount()).isEqualTo(0);
        assertThat(aggregate.getTwoPointCount()).isEqualTo(0);
        assertThat(aggregate.getOnePointCount()).isEqualTo(0);
        assertThat(aggregate.getTotalRating()).isEqualTo(5.0f);
        assertThat(aggregate.getAverageRating()).isEqualTo(5.0f);
    }

    @Test
    @DisplayName("addPoint는 5점 카운트를 증가시키고 totalRating/averageRating은 변경하지 않는다")
    void shouldIncrementFivePointCount() {
        ProductReviewAggregate aggregate = createEmptyAggregate();

        aggregate.addPoint(5);

        assertThat(aggregate.getTotalCount()).isEqualTo(1);
        assertThat(aggregate.getFivePointCount()).isEqualTo(1);
        assertThat(aggregate.getTotalRating()).isEqualTo(0.0f);
        assertThat(aggregate.getAverageRating()).isEqualTo(0.0f);
    }

    @Test
    @DisplayName("addPoint는 4점 카운트를 증가시킨다")
    void shouldIncrementFourPointCount() {
        ProductReviewAggregate aggregate = createEmptyAggregate();

        aggregate.addPoint(4);

        assertThat(aggregate.getTotalCount()).isEqualTo(1);
        assertThat(aggregate.getFourPointCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("addPoint는 3점 카운트를 증가시킨다")
    void shouldIncrementThreePointCount() {
        ProductReviewAggregate aggregate = createEmptyAggregate();

        aggregate.addPoint(3);

        assertThat(aggregate.getTotalCount()).isEqualTo(1);
        assertThat(aggregate.getThreePointCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("addPoint는 2점 카운트를 증가시킨다")
    void shouldIncrementTwoPointCount() {
        ProductReviewAggregate aggregate = createEmptyAggregate();

        aggregate.addPoint(2);

        assertThat(aggregate.getTotalCount()).isEqualTo(1);
        assertThat(aggregate.getTwoPointCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("addPoint는 1점 카운트를 증가시킨다")
    void shouldIncrementOnePointCount() {
        ProductReviewAggregate aggregate = createEmptyAggregate();

        aggregate.addPoint(1);

        assertThat(aggregate.getTotalCount()).isEqualTo(1);
        assertThat(aggregate.getOnePointCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("addPoint에 유효하지 않은 점수를 전달하면 InvalidRatingPointException이 발생한다")
    void shouldThrowExceptionWhenAddingInvalidPoint() {
        ProductReviewAggregate aggregate = createEmptyAggregate();

        assertThatThrownBy(() -> aggregate.addPoint(0))
                .isInstanceOf(InvalidRatingPointException.class);
        assertThatThrownBy(() -> aggregate.addPoint(6))
                .isInstanceOf(InvalidRatingPointException.class);
        assertThatThrownBy(() -> aggregate.addPoint(-1))
                .isInstanceOf(InvalidRatingPointException.class);
    }

    @Test
    @DisplayName("reducePoint는 해당 별점 카운트를 감소시킨다")
    void shouldDecrementPointCount() {
        ProductReviewAggregate aggregate = createAggregateWithAllPoints();

        aggregate.reducePoint(5);
        aggregate.reducePoint(3);

        assertThat(aggregate.getTotalCount()).isEqualTo(3);
        assertThat(aggregate.getFivePointCount()).isEqualTo(0);
        assertThat(aggregate.getThreePointCount()).isEqualTo(0);
        assertThat(aggregate.getFourPointCount()).isEqualTo(1);
        assertThat(aggregate.getTwoPointCount()).isEqualTo(1);
        assertThat(aggregate.getOnePointCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("reducePoint에 유효하지 않은 점수를 전달하면 InvalidRatingPointException이 발생한다")
    void shouldThrowExceptionWhenReducingInvalidPoint() {
        ProductReviewAggregate aggregate = createEmptyAggregate();

        assertThatThrownBy(() -> aggregate.reducePoint(0))
                .isInstanceOf(InvalidRatingPointException.class);
        assertThatThrownBy(() -> aggregate.reducePoint(6))
                .isInstanceOf(InvalidRatingPointException.class);
    }

    @Test
    @DisplayName("changePoint는 이전 별점을 감소시키고 새 별점을 증가시킨다")
    void shouldReducePreviousAndAddNewPoint() {
        ProductReviewAggregate aggregate = createAggregateWithAllPoints();

        aggregate.changePoint(5.0f, 1.0f);

        assertThat(aggregate.getTotalCount()).isEqualTo(5);
        assertThat(aggregate.getFivePointCount()).isEqualTo(0);
        assertThat(aggregate.getOnePointCount()).isEqualTo(2);
        assertThat(aggregate.getTotalRating()).isEqualTo(15.0f);
        assertThat(aggregate.getAverageRating()).isEqualTo(3.0f);
    }

    @Test
    @DisplayName("computeRating은 totalRating을 누적하고 averageRating을 계산한다")
    void shouldAccumulateTotalRatingAndComputeAverage() {
        ProductReviewAggregate aggregate = createEmptyAggregate();
        aggregate.addPoint(5);
        aggregate.computeRating(5.0f);
        aggregate.addPoint(3);
        aggregate.computeRating(3.0f);

        assertThat(aggregate.getTotalRating()).isEqualTo(8.0f);
        assertThat(aggregate.getAverageRating()).isEqualTo(4.0f);
    }

    @Test
    @DisplayName("여러 리뷰 추가 후 평균 별점이 정확하게 계산된다")
    void shouldCalculateCorrectAverageAfterMultipleReviews() {
        ProductReviewAggregate aggregate = createEmptyAggregate();

        aggregate.addPoint(5);
        aggregate.computeRating(5.0f);
        aggregate.addPoint(4);
        aggregate.computeRating(4.0f);
        aggregate.addPoint(3);
        aggregate.computeRating(3.0f);

        assertThat(aggregate.getTotalCount()).isEqualTo(3);
        assertThat(aggregate.getTotalRating()).isEqualTo(12.0f);
        assertThat(aggregate.getAverageRating()).isCloseTo(4.0f, within(0.01f));
        assertThat(aggregate.getFivePointCount()).isEqualTo(1);
        assertThat(aggregate.getFourPointCount()).isEqualTo(1);
        assertThat(aggregate.getThreePointCount()).isEqualTo(1);
    }

    private ProductReviewAggregate createEmptyAggregate() {
        return ProductReviewAggregate.from(ProductReviewAggregateSnapshotState.builder()
                .productId(1L)
                .totalCount(0)
                .fivePointCount(0)
                .fourPointCount(0)
                .threePointCount(0)
                .twoPointCount(0)
                .onePointCount(0)
                .totalRating(0.0f)
                .averageRating(0.0f)
                .build());
    }

    private ProductReviewAggregate createAggregateWithAllPoints() {
        return ProductReviewAggregate.from(ProductReviewAggregateSnapshotState.builder()
                .productId(1L)
                .totalCount(5)
                .fivePointCount(1)
                .fourPointCount(1)
                .threePointCount(1)
                .twoPointCount(1)
                .onePointCount(1)
                .totalRating(15.0f)
                .averageRating(3.0f)
                .build());
    }

    private Review createReviewWithRating(Float rating, Long productId) {
        return Review.from(ReviewSnapshotState.builder()
                .id(1L)
                .reviewerId(100L)
                .orderId(200L)
                .productId(productId)
                .reviewerName("홍길동")
                .maskedReviewerName("홍**")
                .rating(rating)
                .content("테스트 리뷰")
                .isPhoto(false)
                .isEdited(false)
                .likeCount(0)
                .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                .build());
    }
}
