package com.personal.marketnote.community.adapter.out.persistence.review;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.review.entity.ProductReviewAggregateJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.review.entity.ReviewJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.review.repository.ProductReviewAggregateJpaRepository;
import com.personal.marketnote.community.adapter.out.persistence.review.repository.ReviewJpaRepository;
import com.personal.marketnote.community.adapter.out.persistence.review.repository.ReviewVersionHistoryJpaRepository;
import com.personal.marketnote.community.domain.review.ProductReviewAggregate;
import com.personal.marketnote.community.domain.review.ProductReviewAggregateSnapshotState;
import com.personal.marketnote.community.domain.review.Rating;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewCreateState;
import com.personal.marketnote.community.domain.review.ReviewSortProperty;
import com.personal.marketnote.community.domain.review.ReviewVersionHistory;
import com.personal.marketnote.community.domain.review.ReviewVersionHistoryCreateState;
import com.personal.marketnote.community.domain.review.Reviews;
import com.personal.marketnote.community.exception.ProductReviewAggregateNotFoundException;
import com.personal.marketnote.community.exception.ReviewNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewPersistenceAdapter")
class ReviewPersistenceAdapterTest {

    @Mock
    private ReviewJpaRepository reviewJpaRepository;

    @Mock
    private ReviewVersionHistoryJpaRepository reviewVersionHistoryJpaRepository;

    @Mock
    private ProductReviewAggregateJpaRepository productReviewAggregateJpaRepository;

    @InjectMocks
    private ReviewPersistenceAdapter adapter;

    private Review sampleReview;

    @BeforeEach
    void setUp() {
        sampleReview = Review.from(
                ReviewCreateState.builder()
                        .reviewerId(10L)
                        .orderId(100L)
                        .productId(1000L)
                        .pricePolicyId(10000L)
                        .productImageUrl("https://cdn.example.com/product.png")
                        .selectedOptions("RED")
                        .quantity(2)
                        .reviewerName("리뷰어")
                        .rating(Rating.of(5f))
                        .content("리뷰 내용")
                        .isPhoto(true)
                        .unitAmount(5000L)
                        .build()
        );
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("리뷰를 저장하고 id를 orderNum에 반영한 도메인을 반환한다")
        void returnsSavedReviewWithOrderNumSetToId() {
            ReviewJpaEntity savedEntity = newPersistedEntity(77L, sampleReview);
            when(reviewJpaRepository.save(any(ReviewJpaEntity.class))).thenReturn(savedEntity);

            Review result = adapter.save(sampleReview);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(77L);
            assertThat(result.getOrderNum()).isEqualTo(77L);
            assertThat(savedEntity.getOrderNum()).isEqualTo(77L);
        }
    }

    @Nested
    @DisplayName("saveAggregate")
    class SaveAggregate {

        @Test
        @DisplayName("집계 도메인을 엔티티로 변환해 저장한다")
        void savesAggregate() {
            ProductReviewAggregate aggregate = ProductReviewAggregate.from(sampleReview);

            adapter.saveAggregate(aggregate);

            ArgumentCaptor<ProductReviewAggregateJpaEntity> captor =
                    ArgumentCaptor.forClass(ProductReviewAggregateJpaEntity.class);
            verify(productReviewAggregateJpaRepository).save(captor.capture());
            assertThat(captor.getValue().getProductId()).isEqualTo(1000L);
            assertThat(captor.getValue().getTotalCount()).isEqualTo(1);
            assertThat(captor.getValue().getFivePointCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("saveVersionHistory")
    class SaveVersionHistory {

        @Test
        @DisplayName("버전 히스토리 도메인을 엔티티로 변환해 저장한다")
        void savesVersionHistory() {
            ReviewVersionHistory history = ReviewVersionHistory.from(
                    ReviewVersionHistoryCreateState.builder()
                            .reviewId(77L)
                            .rating(Rating.of(4f))
                            .content("이전 내용")
                            .isPhoto(false)
                            .build()
            );

            adapter.saveVersionHistory(history);

            verify(reviewVersionHistoryJpaRepository).save(any());
        }
    }

    @Nested
    @DisplayName("existsById / existsByIdAndReviewerId / existsByOrderIdAndPricePolicyId")
    class ExistsMethods {

        @Test
        @DisplayName("existsById를 그대로 위임한다")
        void delegatesExistsById() {
            when(reviewJpaRepository.existsById(5L)).thenReturn(true);

            assertThat(adapter.existsById(5L)).isTrue();
        }

        @Test
        @DisplayName("existsByIdAndReviewerId를 그대로 위임한다")
        void delegatesExistsByIdAndReviewerId() {
            when(reviewJpaRepository.existsByIdAndReviewerId(5L, 10L)).thenReturn(true);

            assertThat(adapter.existsByIdAndReviewerId(5L, 10L)).isTrue();
        }

        @Test
        @DisplayName("existsByOrderIdAndPricePolicyId를 그대로 위임한다")
        void delegatesExistsByOrderIdAndPricePolicyId() {
            when(reviewJpaRepository.existsByOrderIdAndPricePolicyId(100L, 10000L)).thenReturn(true);

            assertThat(adapter.existsByOrderIdAndPricePolicyId(100L, 10000L)).isTrue();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("엔티티가 존재하면 도메인으로 변환해 반환한다")
        void returnsDomainWhenFound() {
            ReviewJpaEntity entity = newPersistedEntity(1L, sampleReview);
            when(reviewJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            Optional<Review> result = adapter.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("엔티티가 없으면 빈 Optional을 반환한다")
        void returnsEmptyWhenNotFound() {
            when(reviewJpaRepository.findById(999L)).thenReturn(Optional.empty());

            assertThat(adapter.findById(999L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findProductReviews")
    class FindProductReviews {

        private final Pageable pageable = PageRequest.of(
                0, 20, Sort.by(Sort.Direction.DESC, "id")
        );

        @Test
        @DisplayName("isPhoto=true이면 포토 리뷰 쿼리로 분기한다")
        void branchesToPhotoQueryWhenIsPhotoTrue() {
            when(reviewJpaRepository.findProductPhotoReviewsByCursor(
                    eq(1000L), eq(50L), eq(pageable), eq("id"), eq(false)
            )).thenReturn(List.of(newPersistedEntity(1L, sampleReview)));

            Reviews result = adapter.findProductReviews(1000L, true, 50L, pageable, ReviewSortProperty.ID);

            assertThat(result.size()).isEqualTo(1);
            verify(reviewJpaRepository, never()).findProductReviewsByCursor(
                    any(), any(), any(), any(), anyBoolean()
            );
        }

        @Test
        @DisplayName("isPhoto=false이면 일반 리뷰 쿼리로 분기한다")
        void branchesToRegularQueryWhenIsPhotoFalse() {
            when(reviewJpaRepository.findProductReviewsByCursor(
                    eq(1000L), eq(null), eq(pageable), eq("id"), eq(false)
            )).thenReturn(List.of());

            Reviews result = adapter.findProductReviews(1000L, false, null, pageable, ReviewSortProperty.ID);

            assertThat(result.size()).isZero();
            verify(reviewJpaRepository, never()).findProductPhotoReviewsByCursor(
                    any(), any(), any(), any(), anyBoolean()
            );
        }

        @Test
        @DisplayName("Pageable에 sortProperty와 일치하는 Order가 있으면 그 방향을 isAsc로 사용한다")
        void usesOrderDirectionFromPageable() {
            Pageable ratingAsc = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "rating"));
            ArgumentCaptor<Boolean> isAscCaptor = ArgumentCaptor.forClass(Boolean.class);
            when(reviewJpaRepository.findProductReviewsByCursor(
                    eq(1000L), any(), eq(ratingAsc), eq("rating"), isAscCaptor.capture()
            )).thenReturn(List.of());

            adapter.findProductReviews(1000L, false, null, ratingAsc, ReviewSortProperty.RATING);

            assertThat(isAscCaptor.getValue()).isTrue();
        }

        @Test
        @DisplayName("sortProperty와 일치하는 Order는 없지만 정렬되어 있으면 첫 Order 방향을 사용한다")
        void fallsBackToFirstOrderDirection() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt"));
            ArgumentCaptor<Boolean> isAscCaptor = ArgumentCaptor.forClass(Boolean.class);
            when(reviewJpaRepository.findProductReviewsByCursor(
                    eq(1000L), any(), eq(pageable), eq("likeCount"), isAscCaptor.capture()
            )).thenReturn(List.of());

            adapter.findProductReviews(1000L, false, null, pageable, ReviewSortProperty.LIKE);

            assertThat(isAscCaptor.getValue()).isTrue();
        }

        @Test
        @DisplayName("Pageable이 unsorted이면 isAsc는 false로 처리한다")
        void defaultsToDescWhenUnsorted() {
            Pageable unsorted = PageRequest.of(0, 10);
            ArgumentCaptor<Boolean> isAscCaptor = ArgumentCaptor.forClass(Boolean.class);
            when(reviewJpaRepository.findProductReviewsByCursor(
                    eq(1000L), any(), eq(unsorted), eq("id"), isAscCaptor.capture()
            )).thenReturn(List.of());

            adapter.findProductReviews(1000L, false, null, unsorted, ReviewSortProperty.ID);

            assertThat(isAscCaptor.getValue()).isFalse();
        }
    }

    @Nested
    @DisplayName("countActive(productId, isPhoto)")
    class CountActiveProduct {

        @Test
        @DisplayName("isPhoto=true이면 포토 전용 count 쿼리를 호출한다")
        void countsPhotoOnlyWhenIsPhotoTrue() {
            when(reviewJpaRepository.countByProductIdAndIsPhoto(1000L, true)).thenReturn(7L);

            assertThat(adapter.countActive(1000L, true)).isEqualTo(7L);
            verify(reviewJpaRepository, never()).countByProductId(any());
        }

        @Test
        @DisplayName("isPhoto=false이면 전체 count 쿼리를 호출한다")
        void countsAllWhenIsPhotoFalse() {
            when(reviewJpaRepository.countByProductId(1000L)).thenReturn(11L);

            assertThat(adapter.countActive(1000L, false)).isEqualTo(11L);
            verify(reviewJpaRepository, never()).countByProductIdAndIsPhoto(any(), any());
        }
    }

    @Nested
    @DisplayName("findProductReviewAggregateByProductId")
    class FindProductReviewAggregateByProductId {

        @Test
        @DisplayName("집계 엔티티가 있으면 도메인으로 변환해 반환한다")
        void returnsDomainWhenAggregateFound() {
            ProductReviewAggregateJpaEntity entity = ProductReviewAggregateJpaEntity.from(
                    ProductReviewAggregate.from(sampleReview)
            );
            when(productReviewAggregateJpaRepository.findByProductId(1000L)).thenReturn(Optional.of(entity));

            Optional<ProductReviewAggregate> result =
                    adapter.findProductReviewAggregateByProductId(1000L);

            assertThat(result).isPresent();
            assertThat(result.get().getProductId()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("집계 엔티티가 없으면 빈 Optional을 반환한다")
        void returnsEmptyWhenAggregateMissing() {
            when(productReviewAggregateJpaRepository.findByProductId(1000L)).thenReturn(Optional.empty());

            assertThat(adapter.findProductReviewAggregateByProductId(1000L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findProductReviewAggregatesByProductIds")
    class FindProductReviewAggregatesByProductIds {

        @Test
        @DisplayName("productIds가 비어 있으면 빈 Map을 반환하고 레포지토리를 호출하지 않는다")
        void returnsEmptyMapAndSkipsRepositoryWhenIdsEmpty() {
            Map<Long, ProductReviewAggregate> result =
                    adapter.findProductReviewAggregatesByProductIds(List.of());

            assertThat(result).isEmpty();
            verifyNoInteractions(productReviewAggregateJpaRepository);
        }

        @Test
        @DisplayName("productIds가 주어지면 productId를 key로 Map을 반환한다")
        void returnsMapKeyedByProductId() {
            ProductReviewAggregateJpaEntity entity1000 = ProductReviewAggregateJpaEntity.from(
                    ProductReviewAggregate.from(sampleReview)
            );
            ProductReviewAggregate other = ProductReviewAggregate.from(
                    ProductReviewAggregateSnapshotState.builder()
                            .productId(2000L)
                            .totalCount(3)
                            .fivePointCount(3)
                            .fourPointCount(0)
                            .threePointCount(0)
                            .twoPointCount(0)
                            .onePointCount(0)
                            .totalRating(15f)
                            .averageRating(5f)
                            .build()
            );
            ProductReviewAggregateJpaEntity entity2000 = ProductReviewAggregateJpaEntity.from(other);
            when(productReviewAggregateJpaRepository.findByProductIdIn(List.of(1000L, 2000L)))
                    .thenReturn(List.of(entity1000, entity2000));

            Map<Long, ProductReviewAggregate> result =
                    adapter.findProductReviewAggregatesByProductIds(List.of(1000L, 2000L));

            assertThat(result).hasSize(2);
            assertThat(result).containsKey(1000L);
            assertThat(result).containsKey(2000L);
        }
    }

    @Nested
    @DisplayName("findUserReviews")
    class FindUserReviews {

        private final Pageable pageable = PageRequest.of(
                0, 20, Sort.by(Sort.Direction.DESC, "id")
        );

        @Test
        @DisplayName("유저 리뷰 쿼리에 sortProperty 기반 파라미터를 전달한다")
        void delegatesToUserCursorQuery() {
            when(reviewJpaRepository.findUserReviewsByCursor(
                    eq(10L), eq(null), eq(pageable), eq("id"), eq(false)
            )).thenReturn(List.of(newPersistedEntity(1L, sampleReview)));

            Reviews result = adapter.findUserReviews(10L, null, pageable, ReviewSortProperty.ID);

            assertThat(result.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("countActive(reviewerId)")
    class CountActiveReviewer {

        @Test
        @DisplayName("reviewerId 기반 count 쿼리를 호출한다")
        void delegatesToRepositoryCount() {
            when(reviewJpaRepository.countByReviewerId(10L)).thenReturn(4L);

            assertThat(adapter.countActive(10L)).isEqualTo(4L);
        }
    }

    @Nested
    @DisplayName("findUserReviewsByOffset")
    class FindUserReviewsByOffset {

        @Test
        @DisplayName("isDesc=true이면 DESC PageRequest로 쿼리를 호출한다")
        void buildsDescPageableWhenIsDesc() {
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            when(reviewJpaRepository.findUserReviewsByOffset(eq(10L), pageableCaptor.capture()))
                    .thenReturn(List.of());

            adapter.findUserReviewsByOffset(10L, 2, 30, true, ReviewSortProperty.ID);

            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getPageSize()).isEqualTo(30);
            Sort.Order order = pageable.getSort().getOrderFor("id");
            assertThat(order).isNotNull();
            assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("isDesc=false이면 ASC PageRequest로 쿼리를 호출한다")
        void buildsAscPageableWhenIsNotDesc() {
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            when(reviewJpaRepository.findUserReviewsByOffset(eq(10L), pageableCaptor.capture()))
                    .thenReturn(List.of(newPersistedEntity(1L, sampleReview)));

            Reviews result = adapter.findUserReviewsByOffset(
                    10L, 1, 10, false, ReviewSortProperty.ORDER_NUM
            );

            assertThat(result.size()).isEqualTo(1);
            Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("orderNum");
            assertThat(order).isNotNull();
            assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
        }
    }

    @Nested
    @DisplayName("update(Review)")
    class UpdateReview {

        @Test
        @DisplayName("엔티티를 조회해 도메인 상태로 업데이트한다")
        void updatesEntityFromDomain() {
            ReviewJpaEntity entity = newPersistedEntity(1L, sampleReview);
            when(reviewJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            sampleReview.update(Rating.of(3f), "수정된 내용", false);
            ReflectionTestUtils.setField(sampleReview, "id", 1L);
            adapter.update(sampleReview);

            assertThat(entity.getRating()).isEqualTo(3.0f);
            assertThat(entity.getContent()).isEqualTo("수정된 내용");
            assertThat(entity.getIsPhoto()).isFalse();
            assertThat(entity.getIsEdited()).isTrue();
        }

        @Test
        @DisplayName("엔티티가 없으면 ReviewNotFoundException을 던진다")
        void throwsWhenReviewMissing() {
            when(reviewJpaRepository.findById(404L)).thenReturn(Optional.empty());
            ReflectionTestUtils.setField(sampleReview, "id", 404L);

            assertThatThrownBy(() -> adapter.update(sampleReview))
                    .isInstanceOf(ReviewNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update(ProductReviewAggregate)")
    class UpdateAggregate {

        @Test
        @DisplayName("엔티티를 조회해 집계 도메인 상태로 업데이트한다")
        void updatesAggregateEntity() {
            ProductReviewAggregate aggregate = ProductReviewAggregate.from(sampleReview);
            ProductReviewAggregateJpaEntity entity = ProductReviewAggregateJpaEntity.from(aggregate);
            when(productReviewAggregateJpaRepository.findByProductId(1000L))
                    .thenReturn(Optional.of(entity));
            aggregate.addPoint(4);
            aggregate.computeRating(4);

            adapter.update(aggregate);

            assertThat(entity.getTotalCount()).isEqualTo(aggregate.getTotalCount());
            assertThat(entity.getFourPointCount()).isEqualTo(aggregate.getFourPointCount());
            assertThat(entity.getAverageRating()).isEqualTo(aggregate.getAverageRating());
        }

        @Test
        @DisplayName("엔티티가 없으면 ProductReviewAggregateNotFoundException을 던진다")
        void throwsWhenAggregateMissing() {
            ProductReviewAggregate aggregate = ProductReviewAggregate.from(sampleReview);
            when(productReviewAggregateJpaRepository.findByProductId(1000L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> adapter.update(aggregate))
                    .isInstanceOf(ProductReviewAggregateNotFoundException.class);
        }
    }

    private ReviewJpaEntity newPersistedEntity(Long id, Review review) {
        ReviewJpaEntity entity = ReviewJpaEntity.from(review);
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "status", EntityStatus.ACTIVE);
        ReflectionTestUtils.setField(entity, "isEdited", false);
        ReflectionTestUtils.setField(entity, "likeCount", 0);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(entity, "modifiedAt", LocalDateTime.now());
        return entity;
    }
}
