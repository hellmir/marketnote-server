package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.like.repository.LikeJpaRepository;
import com.personal.marketnote.community.adapter.out.persistence.review.entity.ReviewJpaEntity;
import com.personal.marketnote.community.domain.like.Like;
import com.personal.marketnote.community.domain.like.LikeCreateState;
import com.personal.marketnote.community.domain.like.LikeTargetType;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewCreateState;
import com.personal.marketnote.community.domain.review.ReviewSnapshotState;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
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
@Import(AuditConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewLikeCountSortQueryTest {

    @Autowired
    private ReviewJpaRepository reviewRepository;

    @Autowired
    private LikeJpaRepository likeRepository;

    @Autowired
    private EntityManager entityManager;

    private static final Long PRODUCT_ID = 100L;
    private static final Long REVIEWER_ID = 1L;
    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 20);

    private ReviewJpaEntity reviewA;
    private ReviewJpaEntity reviewB;
    private ReviewJpaEntity reviewC;
    private ReviewJpaEntity reviewD;
    private ReviewJpaEntity reviewE;

    @BeforeEach
    void setUp() {
        reviewA = saveReview(PRODUCT_ID, REVIEWER_ID, 4.0f, false, 1L, 1L);
        reviewB = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, false, 2L, 2L);
        reviewC = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false, 3L, 3L);
        reviewD = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false, 4L, 4L);
        reviewE = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, false, 5L, 5L);

        saveLikes(reviewA.getId(), 5);
        saveLikes(reviewB.getId(), 3);
        saveLikes(reviewC.getId(), 3);
        saveLikes(reviewD.getId(), 3);

        flushAndClear();
    }

    @Nested
    @DisplayName("상품 리뷰 추천순 내림차순 정렬")
    class ProductReviewLikeCountDesc {

        @Test
        @DisplayName("좋아요 수가 다르면 좋아요 수 내림차순으로 정렬한다")
        void shouldSortByLikeCountDesc() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByLikeCountDesc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results).hasSize(5);
            assertThat(results.get(0).getId()).isEqualTo(reviewA.getId());
            assertThat(results.get(4).getId()).isEqualTo(reviewE.getId());
        }

        @Test
        @DisplayName("좋아요 수가 동일하면 별점 높은순으로 정렬한다")
        void shouldSortByRatingDescWhenLikeCountEqual() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByLikeCountDesc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            List<ReviewJpaEntity> sameLikeCountReviews = results.stream()
                    .filter(r -> List.of(reviewB.getId(), reviewC.getId(), reviewD.getId()).contains(r.getId()))
                    .toList();

            assertThat(sameLikeCountReviews).hasSize(3);
            assertThat(sameLikeCountReviews.get(0).getId()).isEqualTo(reviewB.getId());
        }

        @Test
        @DisplayName("좋아요 수와 별점이 모두 동일하면 최신순(id DESC)으로 정렬한다")
        void shouldSortByIdDescWhenLikeCountAndRatingEqual() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByLikeCountDesc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            List<ReviewJpaEntity> sameRatingReviews = results.stream()
                    .filter(r -> List.of(reviewC.getId(), reviewD.getId()).contains(r.getId()))
                    .toList();

            assertThat(sameRatingReviews).hasSize(2);
            assertThat(sameRatingReviews.get(0).getId()).isGreaterThan(sameRatingReviews.get(1).getId());
        }

        @Test
        @DisplayName("전체 정렬 순서를 검증한다: likeCount DESC → rating DESC → id DESC")
        void shouldSortByFullOrderLikeCountDescRatingDescIdDesc() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByLikeCountDesc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            reviewA.getId(),
                            reviewB.getId(),
                            reviewD.getId(),
                            reviewC.getId(),
                            reviewE.getId()
                    );
        }

        @Test
        @DisplayName("INACTIVE 상태의 리뷰는 조회 결과에서 제외된다")
        void shouldExcludeInactiveReviews() {
            deactivateReview(reviewA.getId());
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByLikeCountDesc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results).hasSize(4);
            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .doesNotContain(reviewA.getId());
        }

        @Test
        @DisplayName("해당 상품의 리뷰가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoReviewsExist() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByLikeCountDesc(
                    999999L, null, DEFAULT_PAGEABLE
            );

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("커서 기반 페이지네이션이 3단계 정렬 조건으로 동작한다")
        void shouldPaginateWithThreeLevelCursor() {
            Pageable pageOf2 = PageRequest.of(0, 2);

            List<ReviewJpaEntity> firstPage = reviewRepository.findProductReviewsByCursorOrderByLikeCountDesc(
                    PRODUCT_ID, null, pageOf2
            );
            assertThat(firstPage).hasSize(2);

            Long cursor = firstPage.get(firstPage.size() - 1).getId();
            List<ReviewJpaEntity> secondPage = reviewRepository.findProductReviewsByCursorOrderByLikeCountDesc(
                    PRODUCT_ID, cursor, pageOf2
            );
            assertThat(secondPage).hasSize(2);
            assertThat(secondPage.get(0).getId()).isEqualTo(reviewD.getId());
        }
    }

    @Nested
    @DisplayName("상품 리뷰 추천순 오름차순 정렬")
    class ProductReviewLikeCountAsc {

        @Test
        @DisplayName("좋아요 수 오름차순으로 정렬하고 동일값은 별점 오름차순으로 정렬한다")
        void shouldSortByLikeCountAscWithRatingAscTieBreaker() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByLikeCountAsc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            reviewE.getId(),
                            reviewC.getId(),
                            reviewD.getId(),
                            reviewB.getId(),
                            reviewA.getId()
                    );
        }

        @Test
        @DisplayName("좋아요 수와 별점이 동일하면 id 오름차순으로 정렬한다")
        void shouldSortByIdAscWhenLikeCountAndRatingEqual() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByLikeCountAsc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            List<ReviewJpaEntity> sameRatingReviews = results.stream()
                    .filter(r -> List.of(reviewC.getId(), reviewD.getId()).contains(r.getId()))
                    .toList();

            assertThat(sameRatingReviews).hasSize(2);
            assertThat(sameRatingReviews.get(0).getId()).isLessThan(sameRatingReviews.get(1).getId());
        }
    }

    @Nested
    @DisplayName("포토 리뷰 추천순 정렬")
    class PhotoReviewLikeCountSort {

        private ReviewJpaEntity photoReviewA;
        private ReviewJpaEntity photoReviewB;
        private ReviewJpaEntity photoReviewC;

        @BeforeEach
        void setUp() {
            photoReviewA = saveReview(PRODUCT_ID, REVIEWER_ID, 4.0f, true, 101L, 101L);
            photoReviewB = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, true, 102L, 102L);
            photoReviewC = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true, 103L, 103L);

            saveLikes(photoReviewA.getId(), 3);
            saveLikes(photoReviewB.getId(), 3);
            saveLikes(photoReviewC.getId(), 1);

            flushAndClear();
        }

        @Test
        @DisplayName("포토 리뷰만 필터링하여 추천순 내림차순 정렬한다: likeCount DESC → rating DESC → id DESC")
        void shouldSortPhotoReviewsByLikeCountDescWithRatingTieBreaker() {
            List<ReviewJpaEntity> results = reviewRepository.findProductPhotoReviewsByCursorOrderByLikeCountDesc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            photoReviewB.getId(),
                            photoReviewA.getId(),
                            photoReviewC.getId()
                    );
        }

        @Test
        @DisplayName("포토 리뷰만 필터링하여 추천순 오름차순 정렬한다: likeCount ASC → rating ASC → id ASC")
        void shouldSortPhotoReviewsByLikeCountAscWithRatingTieBreaker() {
            List<ReviewJpaEntity> results = reviewRepository.findProductPhotoReviewsByCursorOrderByLikeCountAsc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            photoReviewC.getId(),
                            photoReviewA.getId(),
                            photoReviewB.getId()
                    );
        }
    }

    @Nested
    @DisplayName("회원 리뷰 추천순 정렬")
    class UserReviewLikeCountSort {

        private ReviewJpaEntity userReviewA;
        private ReviewJpaEntity userReviewB;
        private ReviewJpaEntity userReviewC;
        private static final Long TARGET_USER_ID = 999L;

        @BeforeEach
        void setUp() {
            userReviewA = saveReview(200L, TARGET_USER_ID, 4.0f, false, 201L, 201L);
            userReviewB = saveReview(201L, TARGET_USER_ID, 5.0f, false, 202L, 202L);
            userReviewC = saveReview(202L, TARGET_USER_ID, 3.0f, false, 203L, 203L);

            saveLikes(userReviewA.getId(), 3);
            saveLikes(userReviewB.getId(), 3);
            saveLikes(userReviewC.getId(), 1);

            flushAndClear();
        }

        @Test
        @DisplayName("회원 리뷰를 추천순 내림차순 정렬한다: likeCount DESC → rating DESC → id DESC")
        void shouldSortUserReviewsByLikeCountDescWithRatingTieBreaker() {
            List<ReviewJpaEntity> results = reviewRepository.findUserReviewsByCursorOrderByLikeCountDesc(
                    TARGET_USER_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            userReviewB.getId(),
                            userReviewA.getId(),
                            userReviewC.getId()
                    );
        }

        @Test
        @DisplayName("회원 리뷰를 추천순 오름차순 정렬한다: likeCount ASC → rating ASC → id ASC")
        void shouldSortUserReviewsByLikeCountAscWithRatingTieBreaker() {
            List<ReviewJpaEntity> results = reviewRepository.findUserReviewsByCursorOrderByLikeCountAsc(
                    TARGET_USER_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            userReviewC.getId(),
                            userReviewA.getId(),
                            userReviewB.getId()
                    );
        }
    }

    @Nested
    @DisplayName("default 메서드 라우팅")
    class DefaultMethodRouting {

        @Test
        @DisplayName("sortProperty가 likeCount이고 isAsc가 false이면 내림차순 쿼리에 위임한다")
        void shouldDelegateToLikeCountDescQuery() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "likeCount", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            reviewA.getId(),
                            reviewB.getId(),
                            reviewD.getId(),
                            reviewC.getId(),
                            reviewE.getId()
                    );
        }

        @Test
        @DisplayName("sortProperty가 likeCount이고 isAsc가 true이면 오름차순 쿼리에 위임한다")
        void shouldDelegateToLikeCountAscQuery() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "likeCount", true
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            reviewE.getId(),
                            reviewC.getId(),
                            reviewD.getId(),
                            reviewB.getId(),
                            reviewA.getId()
                    );
        }
    }

    private ReviewJpaEntity saveReview(Long productId, Long reviewerId, Float rating,
                                       Boolean isPhoto, Long orderId, Long pricePolicyId) {
        Review review = Review.from(ReviewCreateState.builder()
                .reviewerId(reviewerId)
                .orderId(orderId)
                .productId(productId)
                .pricePolicyId(pricePolicyId)
                .productImageUrl("https://example.com/image.jpg")
                .selectedOptions("옵션")
                .quantity(1)
                .reviewerName("테스트사용자")
                .rating(rating)
                .content("테스트 리뷰 내용입니다")
                .isPhoto(isPhoto)
                .build());
        return reviewRepository.save(ReviewJpaEntity.from(review));
    }

    private void saveLikes(Long reviewId, int count) {
        for (int i = 1; i <= count; i++) {
            Like like = Like.from(LikeCreateState.builder()
                    .targetType(LikeTargetType.REVIEW)
                    .targetId(reviewId)
                    .userId(reviewId * 1000L + i)
                    .build());
            likeRepository.save(LikeJpaEntity.from(like));
        }
    }

    private void deactivateReview(Long reviewId) {
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
