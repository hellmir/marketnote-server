package com.personal.marketnote.product.adapter.out.persistence.productcatalog;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.configuration.ObjectMapperConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.productcatalog.entity.ProductCatalogReadModelJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productcatalog.repository.ProductCatalogReadModelJpaRepository;
import com.personal.marketnote.product.adapter.out.persistence.productcatalog.repository.ProductCatalogReadModelQueryRepositoryImpl;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogItemResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        AuditConfig.class,
        ObjectMapperConfig.class,
        ProductCatalogReadModelPersistenceAdapter.class,
        ProductCatalogReadModelQueryRepositoryImpl.class
})
class ProductCatalogReadModelPersistenceAdapterTest {

    @Autowired
    private ProductCatalogReadModelPersistenceAdapter adapter;

    @Autowired
    private ProductCatalogReadModelJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("upsertCatalog")
    class UpsertCatalog {

        @Test
        @DisplayName("신규 pricePolicyId면 ReadModel 레코드를 insert한다")
        void insertsNewEntry() {
            adapter.upsertCatalog(
                    1L, 100L, 200L, 10L,
                    "상품", "브랜드",
                    10000L, 9000L, BigDecimal.valueOf(10.0),
                    100L, BigDecimal.valueOf(1.0),
                    0L, 0L, 0L, null, null);

            Optional<ProductCatalogReadModelJpaEntity> entity = repository.findByPricePolicyId(1L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getProductId()).isEqualTo(100L);
            assertThat(entity.get().getName()).isEqualTo("상품");
            assertThat(entity.get().getBrandName()).isEqualTo("브랜드");
            assertThat(entity.get().getPrice()).isEqualTo(10000L);
            assertThat(entity.get().getDiscountPrice()).isEqualTo(9000L);
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("동일 pricePolicyId로 다시 upsert하면 기존 레코드를 업데이트한다 (멱등)")
        void updatesExistingEntry() {
            adapter.upsertCatalog(
                    1L, 100L, 200L, 10L, "상품A", "브랜드A",
                    10000L, 9000L, BigDecimal.valueOf(10.0), 100L, BigDecimal.valueOf(1.0), 0L, 0L, 0L, null, null);

            adapter.upsertCatalog(
                    1L, 100L, 200L, 20L, "상품B", "브랜드B",
                    12000L, 10000L, BigDecimal.valueOf(16.6), 200L, BigDecimal.valueOf(2.0), 5L, 10L, 0L, null, null);

            Optional<ProductCatalogReadModelJpaEntity> entity = repository.findByPricePolicyId(1L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getName()).isEqualTo("상품B");
            assertThat(entity.get().getBrandName()).isEqualTo("브랜드B");
            assertThat(entity.get().getPrimaryCategoryId()).isEqualTo(20L);
            assertThat(entity.get().getPrice()).isEqualTo(12000L);
            assertThat(entity.get().getDiscountPrice()).isEqualTo(10000L);
            assertThat(entity.get().getOrderNum()).isEqualTo(5L);
            assertThat(entity.get().getPopularity()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("updateStockQuantity")
    class UpdateStockQuantity {

        @Test
        @DisplayName("기존 레코드의 재고 수량을 부분 업데이트한다")
        void updatesStockQuantity() {
            adapter.upsertCatalog(
                    1L, 100L, 200L, 10L, "상품", "브랜드",
                    10000L, 9000L, BigDecimal.valueOf(10.0), 100L, BigDecimal.valueOf(1.0), 0L, 0L, 0L, null, null);

            adapter.updateStockQuantity(1L, 77);

            Optional<ProductCatalogReadModelJpaEntity> entity = repository.findByPricePolicyId(1L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getStockQuantity()).isEqualTo(77);
        }

        @Test
        @DisplayName("존재하지 않는 pricePolicyId면 아무 일도 하지 않는다")
        void doesNothingWhenMissing() {
            adapter.updateStockQuantity(999L, 50);

            assertThat(repository.findByPricePolicyId(999L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateRepresentativeImage")
    class UpdateRepresentativeImage {

        @Test
        @DisplayName("동일 productId의 여러 pricePolicy 레코드 대표 이미지(+ resized URL JSON) 를 모두 업데이트한다")
        void updatesAllByProductId() {
            adapter.upsertCatalog(
                    1L, 100L, 200L, 10L, "상품", "브랜드",
                    10000L, 9000L, BigDecimal.valueOf(10.0), 100L, BigDecimal.valueOf(1.0), 0L, 0L, 0L, null, null);
            adapter.upsertCatalog(
                    2L, 100L, 200L, 10L, "상품", "브랜드",
                    11000L, 10000L, BigDecimal.valueOf(9.1), 100L, BigDecimal.valueOf(1.0), 0L, 0L, 0L, null, null);

            adapter.updateRepresentativeImage(
                    100L,
                    "https://img.example/new.png",
                    List.of("https://img.example/new-sm.png", "https://img.example/new-md.png")
            );

            ProductCatalogReadModelJpaEntity entity1 = repository.findByPricePolicyId(1L).orElseThrow();
            ProductCatalogReadModelJpaEntity entity2 = repository.findByPricePolicyId(2L).orElseThrow();
            assertThat(entity1.getRepresentativeImageUrl()).isEqualTo("https://img.example/new.png");
            assertThat(entity1.getResizedImageUrls())
                    .contains("new-sm.png")
                    .contains("new-md.png");
            assertThat(entity2.getRepresentativeImageUrl()).isEqualTo("https://img.example/new.png");
        }

        @Test
        @DisplayName("resized URL 목록이 비어있으면 JSON 필드를 null 로 저장한다")
        void storesNullWhenResizedEmpty() {
            adapter.upsertCatalog(
                    1L, 100L, 200L, 10L, "상품", "브랜드",
                    10000L, 9000L, BigDecimal.valueOf(10.0), 100L, BigDecimal.valueOf(1.0), 0L, 0L, 0L, null, null);

            adapter.updateRepresentativeImage(100L, "https://img.example/a.png", List.of());

            assertThat(repository.findByPricePolicyId(1L).orElseThrow().getResizedImageUrls()).isNull();
        }
    }

    @Nested
    @DisplayName("updateReviewAggregate")
    class UpdateReviewAggregate {

        @Test
        @DisplayName("동일 productId의 여러 pricePolicy 레코드 리뷰 집계를 모두 업데이트한다")
        void updatesAllByProductId() {
            adapter.upsertCatalog(
                    1L, 100L, 200L, 10L, "상품", "브랜드",
                    10000L, 9000L, BigDecimal.valueOf(10.0), 100L, BigDecimal.valueOf(1.0), 0L, 0L, 0L, null, null);
            adapter.upsertCatalog(
                    2L, 100L, 200L, 10L, "상품", "브랜드",
                    11000L, 10000L, BigDecimal.valueOf(9.1), 100L, BigDecimal.valueOf(1.0), 0L, 0L, 0L, null, null);

            adapter.updateReviewAggregate(100L, 12, 4.5f);

            assertThat(repository.findByPricePolicyId(1L).get().getReviewCount()).isEqualTo(12);
            assertThat(repository.findByPricePolicyId(1L).get().getReviewAverageRating()).isEqualTo(4.5f);
            assertThat(repository.findByPricePolicyId(2L).get().getReviewCount()).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("deleteByPricePolicyId / deleteByProductId")
    class Delete {

        @Test
        @DisplayName("deleteByPricePolicyId는 해당 레코드를 제거한다")
        void deletesByPricePolicyId() {
            adapter.upsertCatalog(
                    1L, 100L, 200L, 10L, "상품", "브랜드",
                    10000L, 9000L, BigDecimal.valueOf(10.0), 100L, BigDecimal.valueOf(1.0), 0L, 0L, 0L, null, null);

            adapter.deleteByPricePolicyId(1L);

            assertThat(repository.findByPricePolicyId(1L)).isEmpty();
        }

        @Test
        @DisplayName("deleteByProductId는 동일 productId의 모든 레코드를 제거한다")
        void deletesByProductId() {
            adapter.upsertCatalog(
                    1L, 100L, 200L, 10L, "상품", "브랜드",
                    10000L, 9000L, BigDecimal.valueOf(10.0), 100L, BigDecimal.valueOf(1.0), 0L, 0L, 0L, null, null);
            adapter.upsertCatalog(
                    2L, 100L, 200L, 10L, "상품", "브랜드",
                    11000L, 10000L, BigDecimal.valueOf(9.1), 100L, BigDecimal.valueOf(1.0), 0L, 0L, 0L, null, null);

            adapter.deleteByProductId(100L);

            assertThat(repository.findByPricePolicyId(1L)).isEmpty();
            assertThat(repository.findByPricePolicyId(2L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findCatalogItems / countCatalogItems")
    class FindCatalogItems {

        @Test
        @DisplayName("discountPrice 오름차순 정렬을 반환한다")
        void returnsSortedByDiscountPriceAsc() {
            adapter.upsertCatalog(1L, 100L, 200L, null, "A", null,
                    10000L, 9000L, BigDecimal.valueOf(10.0), 0L, BigDecimal.ZERO, 0L, 0L, 0L, null, null);
            adapter.upsertCatalog(2L, 101L, 200L, null, "B", null,
                    8000L, 7000L, BigDecimal.valueOf(12.5), 0L, BigDecimal.ZERO, 0L, 0L, 0L, null, null);
            adapter.upsertCatalog(3L, 102L, 200L, null, "C", null,
                    15000L, 12000L, BigDecimal.valueOf(20.0), 0L, BigDecimal.ZERO, 0L, 0L, 0L, null, null);

            List<ProductCatalogItemResult> results = adapter.findCatalogItems(
                    null, null, null, 10, true, ProductSortProperty.DISCOUNT_PRICE, null, null
            );

            assertThat(results).hasSize(3);
            assertThat(results.get(0).discountPrice()).isEqualTo(7000L);
            assertThat(results.get(1).discountPrice()).isEqualTo(9000L);
            assertThat(results.get(2).discountPrice()).isEqualTo(12000L);
        }

        @Test
        @DisplayName("카테고리 필터 + 검색어로 조회한다")
        void filtersByCategoryAndSearch() {
            adapter.upsertCatalog(1L, 100L, 200L, 10L, "사과", "브랜드A",
                    10000L, 9000L, BigDecimal.valueOf(10.0), 0L, BigDecimal.ZERO, 0L, 0L, 0L, null, null);
            adapter.upsertCatalog(2L, 101L, 200L, 10L, "배", "브랜드B",
                    8000L, 7000L, BigDecimal.valueOf(12.5), 0L, BigDecimal.ZERO, 0L, 0L, 0L, null, null);
            adapter.upsertCatalog(3L, 102L, 200L, 20L, "사과파이", "브랜드C",
                    15000L, 12000L, BigDecimal.valueOf(20.0), 0L, BigDecimal.ZERO, 0L, 0L, 0L, null, null);

            List<ProductCatalogItemResult> results = adapter.findCatalogItems(
                    10L, null, null, 10, true, ProductSortProperty.ORDER_NUM,
                    ProductSearchTarget.NAME, "사과"
            );

            assertThat(results).hasSize(1);
            assertThat(results.get(0).name()).isEqualTo("사과");
        }

        @Test
        @DisplayName("countCatalogItems는 status=ACTIVE 건수를 반환한다")
        void countsActiveItems() {
            adapter.upsertCatalog(1L, 100L, 200L, null, "A", null,
                    10000L, 9000L, BigDecimal.valueOf(10.0), 0L, BigDecimal.ZERO, 0L, 0L, 0L, null, null);
            adapter.upsertCatalog(2L, 101L, 200L, null, "B", null,
                    8000L, 7000L, BigDecimal.valueOf(12.5), 0L, BigDecimal.ZERO, 0L, 0L, 0L, null, null);

            long count = adapter.countCatalogItems(null, null, null);

            assertThat(count).isEqualTo(2);
        }
    }
}
