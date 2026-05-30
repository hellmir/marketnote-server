package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.review.entity.ReviewJpaEntity;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewCreateState;
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
class ReviewRatingSortQueryTest {

    @Autowired
    private ReviewJpaRepository reviewRepository;

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
        reviewA = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, false, 1L, 1L);
        reviewB = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false, 2L, 2L);
        reviewC = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, false, 3L, 3L);
        reviewD = saveReview(PRODUCT_ID, REVIEWER_ID, 1.0f, false, 4L, 4L);
        reviewE = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, false, 5L, 5L);

        flushAndClear();
    }

    @Nested
    @DisplayName("상품 리뷰 별점 낮은순 정렬")
    class ProductReviewRatingAsc {

        @Test
        @DisplayName("별점이 다르면 별점 오름차순으로 정렬한다")
        void shouldSortByRatingAsc() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByRatingAsc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results).hasSize(5);
            assertThat(results.get(0).getId()).isEqualTo(reviewD.getId());
            assertThat(results.get(4).getId()).isEqualTo(reviewA.getId());
        }

        @Test
        @DisplayName("별점이 동일하면 최신순(id DESC)으로 정렬한다")
        void shouldSortByIdDescWhenRatingEqual() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByRatingAsc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            List<ReviewJpaEntity> sameRatingReviews = results.stream()
                    .filter(r -> List.of(reviewB.getId(), reviewC.getId()).contains(r.getId()))
                    .toList();

            assertThat(sameRatingReviews).hasSize(2);
            assertThat(sameRatingReviews.get(0).getId()).isEqualTo(reviewC.getId());
            assertThat(sameRatingReviews.get(1).getId()).isEqualTo(reviewB.getId());
        }

        @Test
        @DisplayName("전체 정렬 순서를 검증한다: rating ASC → id DESC")
        void shouldSortByFullOrderRatingAscIdDesc() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByRatingAsc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            reviewD.getId(),
                            reviewC.getId(),
                            reviewB.getId(),
                            reviewE.getId(),
                            reviewA.getId()
                    );
        }

        @Test
        @DisplayName("INACTIVE 상태의 리뷰는 조회 결과에서 제외된다")
        void shouldExcludeInactiveReviews() {
            deactivateReview(reviewD.getId());
            flushAndClear();

            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByRatingAsc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results).hasSize(4);
            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .doesNotContain(reviewD.getId());
        }

        @Test
        @DisplayName("해당 상품의 리뷰가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoReviewsExist() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByRatingAsc(
                    999999L, null, DEFAULT_PAGEABLE
            );

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("커서 기반 페이지네이션이 2단계 정렬 조건으로 동작한다")
        void shouldPaginateWithTwoLevelCursor() {
            Pageable pageOf2 = PageRequest.of(0, 2);

            List<ReviewJpaEntity> firstPage = reviewRepository.findProductReviewsByCursorOrderByRatingAsc(
                    PRODUCT_ID, null, pageOf2
            );
            assertThat(firstPage).hasSize(2);
            assertThat(firstPage.get(0).getId()).isEqualTo(reviewD.getId());
            assertThat(firstPage.get(1).getId()).isEqualTo(reviewC.getId());

            Long cursor = firstPage.get(firstPage.size() - 1).getId();
            List<ReviewJpaEntity> secondPage = reviewRepository.findProductReviewsByCursorOrderByRatingAsc(
                    PRODUCT_ID, cursor, pageOf2
            );
            assertThat(secondPage).hasSize(2);
            assertThat(secondPage.get(0).getId()).isEqualTo(reviewB.getId());
        }
    }

    @Nested
    @DisplayName("상품 리뷰 별점 높은순 정렬")
    class ProductReviewRatingDesc {

        @Test
        @DisplayName("별점 높은순으로 정렬하고 동일값은 최신순(id DESC)으로 정렬한다")
        void shouldSortByRatingDescWithIdDescTieBreaker() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByRatingDesc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            List<ReviewJpaEntity> sameRatingReviews = results.stream()
                    .filter(r -> List.of(reviewA.getId(), reviewE.getId()).contains(r.getId()))
                    .toList();

            assertThat(sameRatingReviews).hasSize(2);
            assertThat(sameRatingReviews.get(0).getId()).isEqualTo(reviewE.getId());
            assertThat(sameRatingReviews.get(1).getId()).isEqualTo(reviewA.getId());
        }

        @Test
        @DisplayName("전체 정렬 순서를 검증한다: rating DESC → id DESC")
        void shouldSortByFullOrderRatingDescIdDesc() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursorOrderByRatingDesc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            reviewE.getId(),
                            reviewA.getId(),
                            reviewC.getId(),
                            reviewB.getId(),
                            reviewD.getId()
                    );
        }
    }

    @Nested
    @DisplayName("포토 리뷰 별점 정렬")
    class PhotoReviewRatingSort {

        private ReviewJpaEntity photoReviewA;
        private ReviewJpaEntity photoReviewB;
        private ReviewJpaEntity photoReviewC;

        @BeforeEach
        void setUp() {
            photoReviewA = saveReview(PRODUCT_ID, REVIEWER_ID, 5.0f, true, 101L, 101L);
            photoReviewB = saveReview(PRODUCT_ID, REVIEWER_ID, 3.0f, true, 102L, 102L);
            photoReviewC = saveReview(PRODUCT_ID, REVIEWER_ID, 1.0f, true, 103L, 103L);

            flushAndClear();
        }

        @Test
        @DisplayName("포토 리뷰만 필터링하여 별점 낮은순 정렬한다: rating ASC → id DESC")
        void shouldSortPhotoReviewsByRatingAsc() {
            List<ReviewJpaEntity> results = reviewRepository.findProductPhotoReviewsByCursorOrderByRatingAsc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            photoReviewC.getId(),
                            photoReviewB.getId(),
                            photoReviewA.getId()
                    );
        }

        @Test
        @DisplayName("포토 리뷰만 필터링하여 별점 높은순 정렬한다: rating DESC → id DESC")
        void shouldSortPhotoReviewsByRatingDesc() {
            List<ReviewJpaEntity> results = reviewRepository.findProductPhotoReviewsByCursorOrderByRatingDesc(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            photoReviewA.getId(),
                            photoReviewB.getId(),
                            photoReviewC.getId()
                    );
        }
    }

    @Nested
    @DisplayName("회원 리뷰 별점 정렬")
    class UserReviewRatingSort {

        private ReviewJpaEntity userReviewA;
        private ReviewJpaEntity userReviewB;
        private ReviewJpaEntity userReviewC;
        private static final Long TARGET_USER_ID = 999L;

        @BeforeEach
        void setUp() {
            userReviewA = saveReview(200L, TARGET_USER_ID, 5.0f, false, 201L, 201L);
            userReviewB = saveReview(201L, TARGET_USER_ID, 3.0f, false, 202L, 202L);
            userReviewC = saveReview(202L, TARGET_USER_ID, 1.0f, false, 203L, 203L);

            flushAndClear();
        }

        @Test
        @DisplayName("회원 리뷰를 별점 낮은순 정렬한다: rating ASC → id DESC")
        void shouldSortUserReviewsByRatingAsc() {
            List<ReviewJpaEntity> results = reviewRepository.findUserReviewsByCursorOrderByRatingAsc(
                    TARGET_USER_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            userReviewC.getId(),
                            userReviewB.getId(),
                            userReviewA.getId()
                    );
        }

        @Test
        @DisplayName("회원 리뷰를 별점 높은순 정렬한다: rating DESC → id DESC")
        void shouldSortUserReviewsByRatingDesc() {
            List<ReviewJpaEntity> results = reviewRepository.findUserReviewsByCursorOrderByRatingDesc(
                    TARGET_USER_ID, null, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            userReviewA.getId(),
                            userReviewB.getId(),
                            userReviewC.getId()
                    );
        }
    }

    @Nested
    @DisplayName("default 메서드 라우팅")
    class DefaultMethodRouting {

        @Test
        @DisplayName("sortProperty가 rating이고 isAsc가 true이면 오름차순 쿼리에 위임한다")
        void shouldDelegateToRatingAscQuery() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "rating", true
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            reviewD.getId(),
                            reviewC.getId(),
                            reviewB.getId(),
                            reviewE.getId(),
                            reviewA.getId()
                    );
        }

        @Test
        @DisplayName("sortProperty가 rating이고 isAsc가 false이면 내림차순 쿼리에 위임한다")
        void shouldDelegateToRatingDescQuery() {
            List<ReviewJpaEntity> results = reviewRepository.findProductReviewsByCursor(
                    PRODUCT_ID, null, DEFAULT_PAGEABLE, "rating", false
            );

            assertThat(results)
                    .extracting(ReviewJpaEntity::getId)
                    .containsExactly(
                            reviewE.getId(),
                            reviewA.getId(),
                            reviewC.getId(),
                            reviewB.getId(),
                            reviewD.getId()
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
