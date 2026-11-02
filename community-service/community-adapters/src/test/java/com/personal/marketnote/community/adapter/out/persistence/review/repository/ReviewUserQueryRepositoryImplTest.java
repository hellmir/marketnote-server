package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.configuration.QuerydslConfig;
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
import org.springframework.data.domain.Sort;
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
class ReviewUserQueryRepositoryImplTest {

    @Autowired
    private ReviewJpaRepository reviewRepository;

    @Autowired
    private EntityManager entityManager;

    private static final Long USER_ID = 42L;
    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 20);

    @Nested
    @DisplayName("findUserReviewsByCursor - id 기준 정렬")
    class UserReviewsByIdSort {

        @Test
        @DisplayName("sortProperty=id, isAsc=true이면 해당 사용자의 리뷰를 id ASC로 반환한다")
        void returnsIdAscending() {
            ReviewJpaEntity a = saveReview(101L, USER_ID, 3.0f, false);
            ReviewJpaEntity b = saveReview(102L, USER_ID, 3.0f, false);
            saveReview(103L, 999L, 3.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findUserReviewsByCursor(
                    USER_ID, null, DEFAULT_PAGEABLE, "id", true
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(a.getId(), b.getId());
        }

        @Test
        @DisplayName("sortProperty=id, isAsc=false이면 id DESC로 반환한다")
        void returnsIdDescending() {
            ReviewJpaEntity a = saveReview(101L, USER_ID, 3.0f, false);
            ReviewJpaEntity b = saveReview(102L, USER_ID, 3.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findUserReviewsByCursor(
                    USER_ID, null, DEFAULT_PAGEABLE, "id", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(b.getId(), a.getId());
        }

        @Test
        @DisplayName("사용자 리뷰 조회는 ACTIVE 상태만 포함한다")
        void excludesInactiveForUser() {
            ReviewJpaEntity active = saveReview(101L, USER_ID, 3.0f, false);
            ReviewJpaEntity inactive = saveReview(102L, USER_ID, 3.0f, false);
            deactivate(inactive.getId());
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findUserReviewsByCursor(
                    USER_ID, null, DEFAULT_PAGEABLE, "id", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(active.getId());
        }
    }

    @Nested
    @DisplayName("findUserReviewsByCursor - orderNum 기준 정렬")
    class UserReviewsByOrderNumSort {

        @Test
        @DisplayName("sortProperty=orderNum, isAsc=true이면 orderNum ASC, id ASC로 정렬한다")
        void sortsByOrderNumAsc() {
            ReviewJpaEntity a = saveReview(101L, USER_ID, 3.0f, false);
            ReviewJpaEntity b = saveReview(102L, USER_ID, 3.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findUserReviewsByCursor(
                    USER_ID, null, DEFAULT_PAGEABLE, "orderNum", true
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(a.getId(), b.getId());
        }

        @Test
        @DisplayName("sortProperty=orderNum, isAsc=false이면 orderNum DESC, id DESC로 정렬한다")
        void sortsByOrderNumDesc() {
            ReviewJpaEntity a = saveReview(101L, USER_ID, 3.0f, false);
            ReviewJpaEntity b = saveReview(102L, USER_ID, 3.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findUserReviewsByCursor(
                    USER_ID, null, DEFAULT_PAGEABLE, "orderNum", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(b.getId(), a.getId());
        }
    }

    @Nested
    @DisplayName("findUserReviewsByCursor - rating 기준 정렬")
    class UserReviewsByRatingSort {

        @Test
        @DisplayName("sortProperty=rating, isAsc=true이면 rating ASC, id DESC로 정렬한다")
        void sortsByRatingAscIdDesc() {
            ReviewJpaEntity r5 = saveReview(101L, USER_ID, 5.0f, false);
            ReviewJpaEntity r1 = saveReview(102L, USER_ID, 1.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findUserReviewsByCursor(
                    USER_ID, null, DEFAULT_PAGEABLE, "rating", true
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(r1.getId(), r5.getId());
        }

        @Test
        @DisplayName("sortProperty=rating, isAsc=false이면 rating DESC, id DESC로 정렬한다")
        void sortsByRatingDescIdDesc() {
            ReviewJpaEntity r5a = saveReview(101L, USER_ID, 5.0f, false);
            ReviewJpaEntity r5b = saveReview(102L, USER_ID, 5.0f, false);
            ReviewJpaEntity r1 = saveReview(103L, USER_ID, 1.0f, false);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findUserReviewsByCursor(
                    USER_ID, null, DEFAULT_PAGEABLE, "rating", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(r5b.getId(), r5a.getId(), r1.getId());
        }
    }

    @Nested
    @DisplayName("findUserReviewsByCursor - likeCount 기준 정렬")
    class UserReviewsByLikeCountSort {

        @Test
        @DisplayName("sortProperty=likeCount, isAsc=false이면 likeCount DESC로 정렬한다")
        void sortsByLikeCountDesc() {
            ReviewJpaEntity top = saveReview(101L, USER_ID, 3.0f, false);
            ReviewJpaEntity bot = saveReview(102L, USER_ID, 3.0f, false);
            addLikes(top.getId(), 3);
            addLikes(bot.getId(), 1);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findUserReviewsByCursor(
                    USER_ID, null, DEFAULT_PAGEABLE, "likeCount", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(top.getId(), bot.getId());
        }

        @Test
        @DisplayName("sortProperty=likeCount, isAsc=true이면 likeCount ASC로 정렬한다")
        void sortsByLikeCountAsc() {
            ReviewJpaEntity top = saveReview(101L, USER_ID, 3.0f, false);
            ReviewJpaEntity bot = saveReview(102L, USER_ID, 3.0f, false);
            addLikes(top.getId(), 3);
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findUserReviewsByCursor(
                    USER_ID, null, DEFAULT_PAGEABLE, "likeCount", true
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(bot.getId(), top.getId());
        }
    }

    @Nested
    @DisplayName("findUserReviewsByOffset - offset 기반 조회")
    class UserReviewsByOffset {

        @Test
        @DisplayName("Pageable의 Sort(id DESC)에 맞춰 offset/limit가 적용된다")
        void returnsByOffsetIdDesc() {
            ReviewJpaEntity a = saveReview(101L, USER_ID, 3.0f, false);
            ReviewJpaEntity b = saveReview(102L, USER_ID, 3.0f, false);
            ReviewJpaEntity c = saveReview(103L, USER_ID, 3.0f, false);
            flushAndClear();

            Pageable pageOf2Desc = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "id"));
            List<ReviewJpaEntity> firstPage = reviewRepository.findUserReviewsByOffset(USER_ID, pageOf2Desc);

            assertThat(firstPage)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(c.getId(), b.getId());

            Pageable pageOf2DescSecond = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "id"));
            List<ReviewJpaEntity> secondPage = reviewRepository.findUserReviewsByOffset(
                    USER_ID, pageOf2DescSecond
            );

            assertThat(secondPage)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(a.getId());
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
                .setParameter("status", com.personal.marketnote.common.domain.EntityStatus.INACTIVE)
                .setParameter("id", reviewId)
                .executeUpdate();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
