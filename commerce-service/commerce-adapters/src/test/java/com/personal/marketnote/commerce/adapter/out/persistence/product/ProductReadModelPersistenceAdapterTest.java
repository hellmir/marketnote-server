package com.personal.marketnote.commerce.adapter.out.persistence.product;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.commerce.adapter.out.persistence.product.entity.ProductReadModelJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.product.repository.ProductReadModelJpaRepository;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
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
@Import({AuditConfig.class, ProductReadModelPersistenceAdapter.class})
@DisplayName("ProductReadModelPersistenceAdapter 테스트")
class ProductReadModelPersistenceAdapterTest {

    @Autowired
    private ProductReadModelPersistenceAdapter adapter;

    @Autowired
    private ProductReadModelJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("upsert")
    class Upsert {

        @Test
        @DisplayName("신규 상품을 저장한다")
        void insertsNewProduct() {
            // when
            adapter.upsert(100L, 1L, 10L, "테스트 상품", "테스트 브랜드", 10000L, 8000L, 100L);

            // then
            Optional<ProductReadModelJpaEntity> entity = repository.findByPricePolicyId(100L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getPricePolicyId()).isEqualTo(100L);
            assertThat(entity.get().getProductId()).isEqualTo(1L);
            assertThat(entity.get().getSellerId()).isEqualTo(10L);
            assertThat(entity.get().getName()).isEqualTo("테스트 상품");
            assertThat(entity.get().getBrandName()).isEqualTo("테스트 브랜드");
            assertThat(entity.get().getPrice()).isEqualTo(10000L);
            assertThat(entity.get().getDiscountPrice()).isEqualTo(8000L);
            assertThat(entity.get().getAccumulatedPoint()).isEqualTo(100L);
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("동일한 pricePolicyId로 upsert 시 기존 데이터를 업데이트한다")
        void updatesExistingProduct() {
            // given
            adapter.upsert(100L, 1L, 10L, "테스트 상품", "테스트 브랜드", 10000L, 8000L, 100L);

            // when
            adapter.upsert(100L, 1L, 10L, "수정된 상품", "수정된 브랜드", 20000L, 15000L, 200L);

            // then
            Optional<ProductReadModelJpaEntity> entity = repository.findByPricePolicyId(100L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getName()).isEqualTo("수정된 상품");
            assertThat(entity.get().getBrandName()).isEqualTo("수정된 브랜드");
            assertThat(entity.get().getPrice()).isEqualTo(20000L);
            assertThat(entity.get().getDiscountPrice()).isEqualTo(15000L);
            assertThat(entity.get().getAccumulatedPoint()).isEqualTo(200L);
        }

        @Test
        @DisplayName("비활성화된 상품에 대해 upsert 시 다시 활성화된다")
        void reactivatesInactiveProduct() {
            // given
            adapter.upsert(100L, 1L, 10L, "테스트 상품", "테스트 브랜드", 10000L, 8000L, 100L);
            adapter.deactivateByPricePolicyId(100L);

            // when
            adapter.upsert(100L, 1L, 10L, "수정된 상품", "수정된 브랜드", 20000L, 15000L, 200L);

            // then
            Optional<ProductReadModelJpaEntity> entity = repository.findByPricePolicyId(100L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getName()).isEqualTo("수정된 상품");
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("nullable 필드가 null이어도 정상 저장된다")
        void insertsWithNullableFields() {
            // when
            adapter.upsert(100L, 1L, 10L, "테스트 상품", null, null, null, null);

            // then
            Optional<ProductReadModelJpaEntity> entity = repository.findByPricePolicyId(100L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getBrandName()).isNull();
            assertThat(entity.get().getPrice()).isNull();
            assertThat(entity.get().getDiscountPrice()).isNull();
            assertThat(entity.get().getAccumulatedPoint()).isNull();
        }
    }

    @Nested
    @DisplayName("deactivateByPricePolicyId")
    class DeactivateByPricePolicyId {

        @Test
        @DisplayName("상품을 비활성화한다")
        void deactivatesProduct() {
            // given
            adapter.upsert(100L, 1L, 10L, "테스트 상품", "테스트 브랜드", 10000L, 8000L, 100L);

            // when
            adapter.deactivateByPricePolicyId(100L);

            // then
            List<ProductReadModelJpaEntity> all = repository.findAll();
            assertThat(all).hasSize(1);
            assertThat(all.getFirst().getStatus()).isEqualTo(EntityStatus.INACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 pricePolicyId 비활성화 시 에러 없이 무시한다")
        void ignoresNonExistentPricePolicyId() {
            // when & then — no exception
            adapter.deactivateByPricePolicyId(999L);
        }
    }

    @Nested
    @DisplayName("findByPricePolicyIds")
    class FindByPricePolicyIds {

        @Test
        @DisplayName("ACTIVE 상태의 상품만 조회한다")
        void returnsOnlyActiveProducts() {
            // given
            adapter.upsert(100L, 1L, 10L, "상품1", "브랜드1", 10000L, 8000L, 100L);
            adapter.upsert(200L, 2L, 10L, "상품2", "브랜드2", 20000L, 15000L, 200L);
            adapter.deactivateByPricePolicyId(200L);

            // when
            Map<Long, ProductInfoResult> result = adapter.findByPricePolicyIds(List.of(100L, 200L));

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsKey(100L);
            assertThat(result).doesNotContainKey(200L);
        }

        @Test
        @DisplayName("pricePolicyId 목록으로 상품 정보를 조회하여 Map으로 반환한다")
        void returnsProductInfoResultMap() {
            // given
            adapter.upsert(100L, 1L, 10L, "상품1", "브랜드1", 10000L, 8000L, 100L);
            adapter.upsert(200L, 2L, 20L, "상품2", "브랜드2", 20000L, 15000L, 200L);

            // when
            Map<Long, ProductInfoResult> result = adapter.findByPricePolicyIds(List.of(100L, 200L));

            // then
            assertThat(result).hasSize(2);

            ProductInfoResult product1 = result.get(100L);
            assertThat(product1.id()).isEqualTo(1L);
            assertThat(product1.sellerId()).isEqualTo(10L);
            assertThat(product1.name()).isEqualTo("상품1");
            assertThat(product1.brandName()).isEqualTo("브랜드1");
            assertThat(product1.price()).isEqualTo(10000L);
            assertThat(product1.discountPrice()).isEqualTo(8000L);
            assertThat(product1.accumulatedPoint()).isEqualTo(100L);
            assertThat(product1.selectedOptions()).isEmpty();

            ProductInfoResult product2 = result.get(200L);
            assertThat(product2.id()).isEqualTo(2L);
            assertThat(product2.sellerId()).isEqualTo(20L);
            assertThat(product2.name()).isEqualTo("상품2");
        }

        @Test
        @DisplayName("빈 pricePolicyId 목록이면 빈 Map을 반환한다")
        void returnsEmptyMapForEmptyInput() {
            // when
            Map<Long, ProductInfoResult> result = adapter.findByPricePolicyIds(List.of());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null pricePolicyId 목록이면 빈 Map을 반환한다")
        void returnsEmptyMapForNullInput() {
            // when
            Map<Long, ProductInfoResult> result = adapter.findByPricePolicyIds(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 pricePolicyId는 결과에 포함되지 않는다")
        void excludesNonExistentPricePolicyIds() {
            // given
            adapter.upsert(100L, 1L, 10L, "상품1", "브랜드1", 10000L, 8000L, 100L);

            // when
            Map<Long, ProductInfoResult> result = adapter.findByPricePolicyIds(List.of(100L, 999L));

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsKey(100L);
            assertThat(result).doesNotContainKey(999L);
        }
    }

    @Nested
    @DisplayName("updateNameByProductId")
    class UpdateNameByProductId {

        @Test
        @DisplayName("productId로 조회하여 이름을 업데이트한다")
        void updatesNameByProductId() {
            // given
            adapter.upsert(100L, 1L, 10L, "원래 상품명", "브랜드", 10000L, 8000L, 100L);

            // when
            adapter.updateNameByProductId(1L, "수정된 상품명");

            // then
            Optional<ProductReadModelJpaEntity> entity = repository.findByPricePolicyId(100L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getName()).isEqualTo("수정된 상품명");
        }

        @Test
        @DisplayName("동일 productId를 가진 여러 Read Model이 있으면 모두 업데이트한다")
        void updatesAllReadModelsWithSameProductId() {
            // given
            adapter.upsert(100L, 1L, 10L, "상품명", "브랜드", 10000L, 8000L, 100L);
            adapter.upsert(200L, 1L, 10L, "상품명", "브랜드", 20000L, 15000L, 200L);

            // when
            adapter.updateNameByProductId(1L, "수정된 상품명");

            // then
            Optional<ProductReadModelJpaEntity> entity1 = repository.findByPricePolicyId(100L);
            Optional<ProductReadModelJpaEntity> entity2 = repository.findByPricePolicyId(200L);
            assertThat(entity1).isPresent();
            assertThat(entity2).isPresent();
            assertThat(entity1.get().getName()).isEqualTo("수정된 상품명");
            assertThat(entity2.get().getName()).isEqualTo("수정된 상품명");
        }

        @Test
        @DisplayName("존재하지 않는 productId 업데이트 시 에러 없이 무시한다")
        void ignoresNonExistentProductId() {
            // when & then — no exception
            adapter.updateNameByProductId(999L, "수정된 상품명");
        }
    }
}
