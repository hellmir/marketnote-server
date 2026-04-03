package com.personal.marketnote.product.adapter.out.persistence.review;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.product.adapter.out.persistence.review.entity.ReviewAggregateReadModelJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.review.repository.ReviewAggregateReadModelJpaRepository;
import com.personal.marketnote.product.port.out.result.ProductReviewAggregateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, ReviewAggregateReadModelPersistenceAdapter.class})
class ReviewAggregateReadModelPersistenceAdapterTest {

    @Autowired
    private ReviewAggregateReadModelPersistenceAdapter adapter;

    @Autowired
    private ReviewAggregateReadModelJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("upsert")
    class Upsert {

        @Test
        @DisplayName("신규 상품의 리뷰 집계를 저장한다")
        void insertsNewReviewAggregate() {
            // when
            adapter.upsert(100L, 10, 4.5f);

            // then
            Optional<ReviewAggregateReadModelJpaEntity> entity = repository.findByProductId(100L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getProductId()).isEqualTo(100L);
            assertThat(entity.get().getTotalCount()).isEqualTo(10);
            assertThat(entity.get().getAverageRating()).isEqualTo(4.5f);
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("동일한 productId로 upsert 시 기존 데이터를 업데이트한다")
        void updatesExistingReviewAggregate() {
            // given
            adapter.upsert(100L, 10, 4.5f);

            // when
            adapter.upsert(100L, 11, 4.6f);

            // then
            Optional<ReviewAggregateReadModelJpaEntity> entity = repository.findByProductId(100L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getTotalCount()).isEqualTo(11);
            assertThat(entity.get().getAverageRating()).isEqualTo(4.6f);
        }

        @Test
        @DisplayName("동일한 값으로 upsert 시 멱등하게 처리된다")
        void idempotentUpsert() {
            // given
            adapter.upsert(100L, 10, 4.5f);

            // when
            adapter.upsert(100L, 10, 4.5f);

            // then
            Optional<ReviewAggregateReadModelJpaEntity> entity = repository.findByProductId(100L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getTotalCount()).isEqualTo(10);
            assertThat(entity.get().getAverageRating()).isEqualTo(4.5f);
        }
    }

    @Nested
    @DisplayName("findByProductIds")
    class FindByProductIds {

        @Test
        @DisplayName("ACTIVE 상태의 리뷰 집계를 productId 목록으로 조회한다")
        void returnsActiveReviewAggregates() {
            // given
            adapter.upsert(100L, 10, 4.5f);
            adapter.upsert(200L, 5, 3.8f);
            adapter.upsert(300L, 20, 4.9f);

            // when
            Map<Long, ProductReviewAggregateResult> result =
                    adapter.findByProductIds(List.of(100L, 200L, 300L));

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(100L).totalCount()).isEqualTo(10);
            assertThat(result.get(100L).averageRating()).isEqualTo(4.5f);
            assertThat(result.get(200L).totalCount()).isEqualTo(5);
            assertThat(result.get(200L).averageRating()).isEqualTo(3.8f);
            assertThat(result.get(300L).totalCount()).isEqualTo(20);
            assertThat(result.get(300L).averageRating()).isEqualTo(4.9f);
        }

        @Test
        @DisplayName("존재하지 않는 productId는 결과에 포함되지 않는다")
        void excludesNonExistentProductIds() {
            // given
            adapter.upsert(100L, 10, 4.5f);

            // when
            Map<Long, ProductReviewAggregateResult> result =
                    adapter.findByProductIds(List.of(100L, 999L));

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsKey(100L);
            assertThat(result).doesNotContainKey(999L);
        }

        @Test
        @DisplayName("빈 productId 목록으로 조회 시 빈 맵을 반환한다")
        void returnsEmptyMapForEmptyProductIds() {
            // when
            Map<Long, ProductReviewAggregateResult> result =
                    adapter.findByProductIds(List.of());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null productId 목록으로 조회 시 빈 맵을 반환한다")
        void returnsEmptyMapForNullProductIds() {
            // when
            Map<Long, ProductReviewAggregateResult> result = adapter.findByProductIds(null);

            // then
            assertThat(result).isEmpty();
        }
    }
}
