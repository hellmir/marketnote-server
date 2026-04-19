package com.personal.marketnote.community.adapter.out.persistence.product;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.community.adapter.out.persistence.product.entity.ProductReadModelJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.product.repository.ProductReadModelJpaRepository;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
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
            adapter.upsert(100L, 1L, 10L, "원래 상품", "원래 브랜드", 10000L, 8000L, 100L);

            // when
            adapter.upsert(100L, 1L, 10L, "변경된 상품", "변경된 브랜드", 20000L, 18000L, 200L);

            // then
            Optional<ProductReadModelJpaEntity> entity = repository.findByPricePolicyId(100L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getName()).isEqualTo("변경된 상품");
            assertThat(entity.get().getBrandName()).isEqualTo("변경된 브랜드");
            assertThat(entity.get().getPrice()).isEqualTo(20000L);
            assertThat(entity.get().getDiscountPrice()).isEqualTo(18000L);
            assertThat(entity.get().getAccumulatedPoint()).isEqualTo(200L);
        }

        @Test
        @DisplayName("비활성화된 상품에 대해 upsert 시 다시 활성화된다")
        void reactivatesInactiveProduct() {
            // given
            adapter.upsert(100L, 1L, 10L, "테스트 상품", "테스트 브랜드", 10000L, 8000L, 100L);
            adapter.deactivateByPricePolicyId(100L);

            // when
            adapter.upsert(100L, 1L, 10L, "테스트 상품", "테스트 브랜드", 10000L, 8000L, 100L);

            // then
            Optional<ProductReadModelJpaEntity> entity = repository.findByPricePolicyId(100L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
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
            Optional<ProductReadModelJpaEntity> entity = repository.findByPricePolicyId(100L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.INACTIVE);
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
        @DisplayName("ACTIVE 상태의 상품을 pricePolicyId별 Map으로 반환한다")
        void returnsActiveProductsAsMap() {
            // given
            adapter.upsert(100L, 1L, 10L, "상품1", "브랜드1", 10000L, 8000L, 100L);
            adapter.upsert(200L, 2L, 20L, "상품2", "브랜드2", 20000L, 18000L, 200L);

            // when
            Map<Long, ProductInfoResult> result = adapter.findByPricePolicyIds(List.of(100L, 200L));

            // then
            assertThat(result).hasSize(2);

            ProductInfoResult product1 = result.get(100L);
            assertThat(product1.name()).isEqualTo("상품1");
            assertThat(product1.sellerId()).isEqualTo(10L);
            assertThat(product1.brandName()).isEqualTo("브랜드1");
            assertThat(product1.pricePolicy().id()).isEqualTo(100L);
            assertThat(product1.pricePolicy().price()).isEqualTo(10000L);
            assertThat(product1.pricePolicy().discountPrice()).isEqualTo(8000L);
            assertThat(product1.pricePolicy().accumulatedPoint()).isEqualTo(100L);
            assertThat(product1.pricePolicy().discountRate()).isNull();
            assertThat(product1.selectedOptions()).isEmpty();
            assertThat(product1.catalogImage()).isNull();

            ProductInfoResult product2 = result.get(200L);
            assertThat(product2.name()).isEqualTo("상품2");
            assertThat(product2.sellerId()).isEqualTo(20L);
        }

        @Test
        @DisplayName("INACTIVE 상태의 상품은 조회하지 않는다")
        void excludesInactiveProducts() {
            // given
            adapter.upsert(100L, 1L, 10L, "상품1", "브랜드1", 10000L, 8000L, 100L);
            adapter.deactivateByPricePolicyId(100L);

            // when
            Map<Long, ProductInfoResult> result = adapter.findByPricePolicyIds(List.of(100L));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("pricePolicyIds가 null이면 빈 Map을 반환한다")
        void returnsEmptyMapWhenNull() {
            // when
            Map<Long, ProductInfoResult> result = adapter.findByPricePolicyIds(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("pricePolicyIds가 빈 리스트이면 빈 Map을 반환한다")
        void returnsEmptyMapWhenEmpty() {
            // when
            Map<Long, ProductInfoResult> result = adapter.findByPricePolicyIds(List.of());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("요청한 pricePolicyId 중 존재하지 않는 것은 결과에 포함되지 않는다")
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
}
