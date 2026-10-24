package com.personal.marketnote.community.domain.review;

import com.personal.marketnote.community.domain.review.exception.InvalidRatingPointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                .totalRating(40L)
                .averageRating(new BigDecimal("4.00"))
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
        assertThat(aggregate.getTotalRating()).isEqualTo(40L);
        assertThat(aggregate.getAverageRating()).isEqualByComparingTo(new BigDecimal("4.00"));
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
        assertThat(aggregate.getTotalRating()).isEqualTo(5L);
        assertThat(aggregate.getAverageRating()).isEqualByComparingTo(new BigDecimal("5.00"));
    }

    @Test
    @DisplayName("addPoint는 5점 카운트를 증가시키고 totalRating/averageRating은 변경하지 않는다")
    void shouldIncrementFivePointCount() {
        ProductReviewAggregate aggregate = createEmptyAggregate();

        aggregate.addPoint(5);

        assertThat(aggregate.getTotalCount()).isEqualTo(1);
        assertThat(aggregate.getFivePointCount()).isEqualTo(1);
        assertThat(aggregate.getTotalRating()).isEqualTo(0L);
        assertThat(aggregate.getAverageRating()).isEqualByComparingTo(BigDecimal.ZERO);
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

        aggregate.changePoint(Rating.of(5.0f), Rating.of(1.0f));

        assertThat(aggregate.getTotalCount()).isEqualTo(5);
        assertThat(aggregate.getFivePointCount()).isEqualTo(0);
        assertThat(aggregate.getOnePointCount()).isEqualTo(2);
        assertThat(aggregate.getTotalRating()).isEqualTo(15L);
        assertThat(aggregate.getAverageRating()).isEqualByComparingTo(new BigDecimal("3.00"));
    }

    @Test
    @DisplayName("computeRating은 totalRating을 누적하고 averageRating을 계산한다")
    void shouldAccumulateTotalRatingAndComputeAverage() {
        ProductReviewAggregate aggregate = createEmptyAggregate();
        aggregate.addPoint(5);
        aggregate.computeRating(5);
        aggregate.addPoint(3);
        aggregate.computeRating(3);

        assertThat(aggregate.getTotalRating()).isEqualTo(8L);
        assertThat(aggregate.getAverageRating()).isEqualByComparingTo(new BigDecimal("4.00"));
    }

    @Test
    @DisplayName("마지막 리뷰 삭제로 totalCount가 0이 되면 totalRating과 averageRating을 0으로 초기화한다")
    void shouldResetRatingsWhenLastReviewDeleted() {
        ProductReviewAggregate aggregate = ProductReviewAggregate.from(ProductReviewAggregateSnapshotState.builder()
                .productId(1L)
                .totalCount(1)
                .fivePointCount(1)
                .fourPointCount(0)
                .threePointCount(0)
                .twoPointCount(0)
                .onePointCount(0)
                .totalRating(5L)
                .averageRating(new BigDecimal("5.00"))
                .build());

        aggregate.reducePoint(5);
        aggregate.computeRating(-5);

        assertThat(aggregate.getTotalCount()).isEqualTo(0);
        assertThat(aggregate.getTotalRating()).isEqualTo(0L);
        assertThat(aggregate.getAverageRating()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("일부 리뷰 삭제 후에도 남은 리뷰의 평균이 정상 계산된다")
    void shouldRecomputeAverageAfterPartialDeletion() {
        ProductReviewAggregate aggregate = createAggregateWithAllPoints();

        aggregate.reducePoint(5);
        aggregate.computeRating(-5);

        assertThat(aggregate.getTotalCount()).isEqualTo(4);
        assertThat(aggregate.getTotalRating()).isEqualTo(10L);
        assertThat(aggregate.getAverageRating()).isEqualByComparingTo(new BigDecimal("2.50"));
    }

    @Test
    @DisplayName("리뷰 추가→삭제→재추가 사이클에서 집계 상태가 일관된다")
    void shouldStayConsistentAcrossAddDeleteAddCycle() {
        ProductReviewAggregate aggregate = createEmptyAggregate();

        aggregate.addPoint(4);
        aggregate.computeRating(4);
        aggregate.reducePoint(4);
        aggregate.computeRating(-4);
        aggregate.addPoint(3);
        aggregate.computeRating(3);

        assertThat(aggregate.getTotalCount()).isEqualTo(1);
        assertThat(aggregate.getTotalRating()).isEqualTo(3L);
        assertThat(aggregate.getAverageRating()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(aggregate.getFourPointCount()).isEqualTo(0);
        assertThat(aggregate.getThreePointCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("여러 리뷰 추가 후 평균 별점이 scale=2 HALF_UP 반올림으로 정확하게 계산된다")
    void shouldCalculateCorrectAverageAfterMultipleReviews() {
        ProductReviewAggregate aggregate = createEmptyAggregate();

        aggregate.addPoint(5);
        aggregate.computeRating(5);
        aggregate.addPoint(4);
        aggregate.computeRating(4);
        aggregate.addPoint(3);
        aggregate.computeRating(3);

        assertThat(aggregate.getTotalCount()).isEqualTo(3);
        assertThat(aggregate.getTotalRating()).isEqualTo(12L);
        // 12/3 = 4.00 정확. scale=2
        assertThat(aggregate.getAverageRating()).isEqualByComparingTo(new BigDecimal("4.00"));
        assertThat(aggregate.getFivePointCount()).isEqualTo(1);
        assertThat(aggregate.getFourPointCount()).isEqualTo(1);
        assertThat(aggregate.getThreePointCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("대량 누적 시 totalRating이 long으로 정확히 보존된다")
    void shouldPreserveExactTotalRatingAfterLargeAccumulation() {
        ProductReviewAggregate aggregate = createEmptyAggregate();

        int count = 100_000;
        for (int i = 0; i < count; i++) {
            aggregate.addPoint(5);
            aggregate.computeRating(5);
        }

        assertThat(aggregate.getTotalCount()).isEqualTo(count);
        assertThat(aggregate.getTotalRating()).isEqualTo(500_000L);
        assertThat(aggregate.getAverageRating()).isEqualByComparingTo(new BigDecimal("5.00"));
    }

    @Test
    @DisplayName("나눗셈이 무한소수가 되면 scale=2 HALF_UP로 반올림된다")
    void shouldRoundToScaleTwoWithHalfUp() {
        ProductReviewAggregate aggregate = createEmptyAggregate();
        aggregate.addPoint(4);
        aggregate.computeRating(4);
        aggregate.addPoint(3);
        aggregate.computeRating(3);
        aggregate.addPoint(3);
        aggregate.computeRating(3);

        assertThat(aggregate.getTotalCount()).isEqualTo(3);
        assertThat(aggregate.getTotalRating()).isEqualTo(10L);
        assertThat(aggregate.getAverageRating()).isEqualByComparingTo(new BigDecimal("3.33"));
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
                .totalRating(0L)
                .averageRating(BigDecimal.ZERO)
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
                .totalRating(15L)
                .averageRating(new BigDecimal("3.00"))
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
                .rating(Rating.of(rating))
                .content("테스트 리뷰")
                .isPhoto(false)
                .isEdited(false)
                .likeCount(0)
                .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                .build());
    }
}
