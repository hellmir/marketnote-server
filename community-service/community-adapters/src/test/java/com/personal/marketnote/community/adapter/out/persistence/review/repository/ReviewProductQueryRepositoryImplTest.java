package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.configuration.QuerydslConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.review.entity.ReviewJpaEntity;
import com.personal.marketnote.community.domain.like.Like;
import com.personal.marketnote.community.domain.like.LikeCreateState;
import com.personal.marketnote.community.domain.like.LikeTargetType;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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
class ReviewProductQueryRepositoryImplTest {

    @Autowired
    private ReviewJpaRepository reviewRepository;

    @Autowired
    private EntityManager entityManager;

    private static final Long PRODUCT_ID = 100L;
    private static final Long REVIEWER_ID = 1L;
    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 20);

    @Nested
    @DisplayName("findProductReviewsByCursor - id 기준 정렬")
    class FindByIdSort {

        @Test
        @DisplayName("sortProperty=id, isAsc=true이면 커서보다 id가 큰 활성 리뷰를 id ASC로 반환한다")
        void returnsIdAscending() {
            ReviewJpaEntity first = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity second = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity third = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, first.getId(), DEFAULT_PAGEABLE, "id", true
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(second.getId(), third.getId());
        }

        @Test
        @DisplayName("sortProperty=id, isAsc=false이면 커서보다 id가 작은 활성 리뷰를 id DESC로 반환한다")
        void returnsIdDescending() {
            ReviewJpaEntity first = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity second = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity third = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, third.getId(), DEFAULT_PAGEABLE, "id", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(second.getId(), first.getId());
        }

        @Test
        @DisplayName("cursor가 null이면 전체 결과를 반환한다")
        void returnsAllWhenCursorIsNull() {
            saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "id", false
            );

            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("INACTIVE 상태의 리뷰는 제외된다")
        void excludesInactive() {
            ReviewJpaEntity active = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity inactive = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            deactivate(inactive.getId());
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "id", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(active.getId());
        }
    }

    @Nested
    @DisplayName("findProductReviewsByCursor - orderNum 기준 정렬")
    class FindByOrderNumSort {

        @Test
        @DisplayName("sortProperty=orderNum, isAsc=true이면 orderNum ASC, id ASC로 정렬한다")
        void sortsByOrderNumAsc() {
            ReviewJpaEntity a = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity b = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity c = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "orderNum", true
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(a.getId(), b.getId(), c.getId());
        }

        @Test
        @DisplayName("sortProperty=orderNum, isAsc=false이면 orderNum DESC, id DESC로 정렬한다")
        void sortsByOrderNumDesc() {
            ReviewJpaEntity a = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity b = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity c = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "orderNum", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(c.getId(), b.getId(), a.getId());
        }

        @Test
        @DisplayName("orderNum 정렬에서 커서 기반 페이지네이션이 동작한다")
        void paginatesWithCursorByOrderNum() {
            ReviewJpaEntity a = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity b = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity c = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, a.getId(), DEFAULT_PAGEABLE, "orderNum", true
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(b.getId(), c.getId());
        }
    }

    @Nested
    @DisplayName("findProductReviewsByCursor - rating 기준 정렬")
    class FindByRatingSort {

        @Test
        @DisplayName("sortProperty=rating, isAsc=true이면 rating ASC, id DESC로 정렬한다")
        void sortsByRatingAscIdDesc() {
            ReviewJpaEntity r5a = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, false);
            ReviewJpaEntity r3a = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity r3b = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity r1a = saveReview(PRODUCT_ID, REVIEWER_ID, 1.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "rating", true
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(r1a.getId(), r3b.getId(), r3a.getId(), r5a.getId());
        }

        @Test
        @DisplayName("sortProperty=rating, isAsc=false이면 rating DESC, id DESC로 정렬한다")
        void sortsByRatingDescIdDesc() {
            ReviewJpaEntity r5a = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, false);
            ReviewJpaEntity r5b = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, false);
            ReviewJpaEntity r1a = saveReview(PRODUCT_ID, REVIEWER_ID, 1.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "rating", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(r5b.getId(), r5a.getId(), r1a.getId());
        }

        @Test
        @DisplayName("rating 커서 페이지네이션이 2단계(rating → id) 정렬 조건으로 동작한다")
        void paginatesByRatingWithTwoStageTieBreak() {
            ReviewJpaEntity r5a = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, false);
            ReviewJpaEntity r5b = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, false);
            ReviewJpaEntity r3a = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity r1a = saveReview(PRODUCT_ID, REVIEWER_ID, 1.0f, false);
            flushAndClear();

            Pageable pageSizeTwo = PageRequest.of(0, 2);
            List<ReviewJpaEntity> firstPage = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, pageSizeTwo, "rating", false
            );

            assertThat(firstPage)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(r5b.getId(), r5a.getId());

            Long cursor = firstPage.get(firstPage.size() - 1).getId();
            List<ReviewJpaEntity> secondPage = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, cursor, pageSizeTwo, "rating", false
            );

            assertThat(secondPage)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(r3a.getId(), r1a.getId());
        }
    }

    @Nested
    @DisplayName("findProductReviewsByCursor - likeCount 기준 정렬")
    class FindByLikeCountSort {

        @Test
        @DisplayName("sortProperty=likeCount, isAsc=false이면 likeCount DESC, rating DESC, id DESC로 정렬한다")
        void sortsByLikeCountDesc() {
            ReviewJpaEntity top = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, false);
            ReviewJpaEntity mid = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity low = saveReview(PRODUCT_ID, REVIEWER_ID, 1.0f, false);
            addLikes(top.getId(), 3);
            addLikes(mid.getId(), 1);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "likeCount", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(top.getId(), mid.getId(), low.getId());
        }

        @Test
        @DisplayName("sortProperty=likeCount, isAsc=true이면 likeCount ASC, rating ASC, id ASC로 정렬한다")
        void sortsByLikeCountAsc() {
            ReviewJpaEntity low = saveReview(PRODUCT_ID, REVIEWER_ID, 1.0f, false);
            ReviewJpaEntity mid = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity top = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, false);
            addLikes(top.getId(), 3);
            addLikes(mid.getId(), 1);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "likeCount", true
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(low.getId(), mid.getId(), top.getId());
        }

        @Test
        @DisplayName("좋아요 수가 같으면 rating으로 2차 정렬한다")
        void tieBreaksByRating() {
            ReviewJpaEntity highRating = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, false);
            ReviewJpaEntity lowRating = saveReview(PRODUCT_ID, REVIEWER_ID, 1.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "likeCount", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(highRating.getId(), lowRating.getId());
        }

        @Test
        @DisplayName("likeCount 커서 페이지네이션이 3단계(likeCount → rating → id) 정렬 조건으로 동작한다")
        void paginatesByLikeCountWithThreeStageTieBreak() {
            ReviewJpaEntity topLikes = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, false);
            ReviewJpaEntity midLikesHighRating = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, false);
            ReviewJpaEntity midLikesLowRatingFirst = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            ReviewJpaEntity midLikesLowRatingSecond = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            addLikes(topLikes.getId(), 3);
            addLikes(midLikesHighRating.getId(), 1);
            addLikes(midLikesLowRatingFirst.getId(), 1);
            addLikes(midLikesLowRatingSecond.getId(), 1);
            flushAndClear();

            Pageable pageSizeTwo = PageRequest.of(0, 2);
            List<ReviewJpaEntity> firstPage = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, pageSizeTwo, "likeCount", false
            );

            assertThat(firstPage)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(topLikes.getId(), midLikesHighRating.getId());

            Long cursor = firstPage.get(firstPage.size() - 1).getId();
            List<ReviewJpaEntity> secondPage = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, cursor, pageSizeTwo, "likeCount", false
            );

            assertThat(secondPage)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(midLikesLowRatingSecond.getId(), midLikesLowRatingFirst.getId());
        }
    }

    @Nested
    @DisplayName("findProductPhotoReviewsByCursor - isPhoto 필터링")
    class FindPhotoReviews {

        @Test
        @DisplayName("isPhoto=true인 리뷰만 반환한다")
        void returnsOnlyPhotoReviews() {
            ReviewJpaEntity photo = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true);
            saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductPhotoReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "id", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(photo.getId());
        }

        @Test
        @DisplayName("포토 리뷰를 rating DESC, id DESC로 정렬한다")
        void sortsPhotoReviewsByRatingDesc() {
            ReviewJpaEntity r1 = saveReview(PRODUCT_ID, REVIEWER_ID, 1.0f, true);
            ReviewJpaEntity r3 = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true);
            ReviewJpaEntity r5 = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, true);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductPhotoReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "rating", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(r5.getId(), r3.getId(), r1.getId());
        }

        @Test
        @DisplayName("포토 리뷰 좋아요순 정렬이 동작한다")
        void sortsPhotoReviewsByLikeCount() {
            ReviewJpaEntity a = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true);
            ReviewJpaEntity b = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true);
            addLikes(b.getId(), 2);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductPhotoReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "likeCount", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(b.getId(), a.getId());
        }

        @Test
        @DisplayName("포토 리뷰 likeCount ASC 정렬이 동작한다")
        void sortsPhotoReviewsByLikeCountAsc() {
            ReviewJpaEntity highRatingNoLikes = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, true);
            ReviewJpaEntity lowRatingNoLikes = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true);
            ReviewJpaEntity withLikes = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true);
            addLikes(withLikes.getId(), 2);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductPhotoReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "likeCount", true
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(lowRatingNoLikes.getId(), highRatingNoLikes.getId(), withLikes.getId());
        }

        @Test
        @DisplayName("포토 리뷰 중 INACTIVE 는 제외된다")
        void excludesInactivePhotoReviews() {
            ReviewJpaEntity active = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true);
            ReviewJpaEntity inactive = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true);
            deactivate(inactive.getId());
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductPhotoReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "id", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(active.getId());
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

    private void addLikes(Long reviewId, int count) {
        for (int i = 0; i < count; i++) {
            long userId = (long) (i + 1) * 1000L + reviewId;
            Like like = Like.from(LikeCreateState.builder()
                    .targetType(LikeTargetType.REVIEW)
                    .targetId(reviewId)
                    .userId(userId)
                    .build());
            entityManager.persist(LikeJpaEntity.from(like));
        }
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
