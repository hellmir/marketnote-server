package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.configuration.QuerydslConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.review.entity.ReviewJpaEntity;
import com.personal.marketnote.community.domain.review.Rating;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewCreateState;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        AuditConfig.class, QuerydslConfig.class,
        ReviewProductQueryRepositoryImpl.class,
        ReviewUserQueryRepositoryImpl.class,
        ReviewStatsQueryRepositoryImpl.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewStatsQueryRepositoryImplTest {

    @Autowired
    private ReviewJpaRepository reviewRepository;

    @Autowired
    private EntityManager entityManager;

    private static final Long PRODUCT_ID = 100L;
    private static final Long REVIEWER_ID = 1L;

    @Nested
    @DisplayName("countByProductId")
    class CountByProductId {

        @Test
        @DisplayName("해당 상품의 활성 리뷰 수만 반환한다")
        void countsActiveOnly() {
            saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true);
            ReviewJpaEntity inactive = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            deactivate(inactive.getId());
            saveReview(999L, REVIEWER_ID, 3.0f, false);
            flushAndClear();

            long count = reviewRepository.countByProductId(PRODUCT_ID);

            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("리뷰가 없으면 0을 반환한다")
        void returnsZeroWhenEmpty() {
            long count = reviewRepository.countByProductId(PRODUCT_ID);

            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("countByProductIdAndIsPhoto")
    class CountByProductIdAndIsPhoto {

        @Test
        @DisplayName("isPhoto=true 조건으로 활성 포토 리뷰만 카운트한다")
        void countsActivePhotoOnly() {
            saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true);
            saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true);
            saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity inactivePhoto = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true);
            deactivate(inactivePhoto.getId());
            flushAndClear();

            long count = reviewRepository.countByProductIdAndIsPhoto(PRODUCT_ID, true);

            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("isPhoto=false 조건으로 활성 일반 리뷰만 카운트한다")
        void countsActiveNonPhotoOnly() {
            saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true);
            saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            flushAndClear();

            long count = reviewRepository.countByProductIdAndIsPhoto(PRODUCT_ID, false);

            assertThat(count).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("countByReviewerId")
    class CountByReviewerId {

        @Test
        @DisplayName("해당 사용자의 전체 리뷰 수를 반환한다(INACTIVE 포함)")
        void countsAllIncludingInactive() {
            saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity inactive = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            deactivate(inactive.getId());
            saveReview(PRODUCT_ID, 999L, 3.0f, false);
            flushAndClear();

            long count = reviewRepository.countByReviewerId(REVIEWER_ID);

            assertThat(count).isEqualTo(3L);
        }

        @Test
        @DisplayName("리뷰가 없으면 0을 반환한다")
        void returnsZeroWhenEmpty() {
            long count = reviewRepository.countByReviewerId(REVIEWER_ID);

            assertThat(count).isZero();
        }
    }

    private ReviewJpaEntity saveReview(Long productId, Long reviewerId, Float rating, Boolean isPhoto) {
        Long orderId = System.nanoTime();
        Long pricePolicyId = System.nanoTime();
        Review review = Review.from(ReviewCreateState.builder()
                .reviewerId(reviewerId)
                .orderId(orderId)
                .productId(productId)
                .pricePolicyId(pricePolicyId)
                .productImageUrl("https://example.com/image.jpg")
                .selectedOptions("옵션")
                .quantity(1)
                .reviewerName("테스트사용자")
                .rating(Rating.of(rating))
                .content("테스트 리뷰 내용입니다")
                .isPhoto(isPhoto)
                .build());
        ReviewJpaEntity saved = reviewRepository.save(ReviewJpaEntity.from(review));
        saved.setIdToOrderNum();
        return saved;
    }

    private void deactivate(Long reviewId) {
        entityManager.createQuery("UPDATE ReviewJpaEntity r SET r.status = :status WHERE r.id = :id")
                .setParameter("status", EntityStatus.INACTIVE)
                .setParameter("id", reviewId)
                .executeUpdate();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
