package com.personal.marketnote.product.adapter.out.persistence.productcatalog.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCatalogReadModelJpaEntityTest {

    private ProductCatalogReadModelJpaEntity newEntity() {
        return ProductCatalogReadModelJpaEntity.of(
                1L, 100L, 200L, 10L,
                "상품A", "브랜드A",
                10000L, 9000L, BigDecimal.valueOf(10.0),
                100L, BigDecimal.valueOf(1.0),
                5L, 7L,
                0L, null, null
        );
    }

    @Nested
    @DisplayName("updateProductInfo")
    class UpdateProductInfo {

        @Test
        @DisplayName("name 이 null 로 들어와도 기존 name 이 유지된다")
        void preservesNameWhenNull() {
            ProductCatalogReadModelJpaEntity entity = newEntity();

            entity.updateProductInfo(null, "브랜드B", 20L);

            assertThat(entity.getName()).isEqualTo("상품A");
            assertThat(entity.getBrandName()).isEqualTo("브랜드B");
            assertThat(entity.getPrimaryCategoryId()).isEqualTo(20L);
        }

        @Test
        @DisplayName("brandName 이 null 로 들어와도 기존 brandName 이 유지된다")
        void preservesBrandNameWhenNull() {
            ProductCatalogReadModelJpaEntity entity = newEntity();

            entity.updateProductInfo("상품B", null, 20L);

            assertThat(entity.getName()).isEqualTo("상품B");
            assertThat(entity.getBrandName()).isEqualTo("브랜드A");
            assertThat(entity.getPrimaryCategoryId()).isEqualTo(20L);
        }

        @Test
        @DisplayName("primaryCategoryId 가 null 로 들어와도 기존 primaryCategoryId 가 유지된다")
        void preservesPrimaryCategoryIdWhenNull() {
            ProductCatalogReadModelJpaEntity entity = newEntity();

            entity.updateProductInfo("상품B", "브랜드B", null);

            assertThat(entity.getName()).isEqualTo("상품B");
            assertThat(entity.getBrandName()).isEqualTo("브랜드B");
            assertThat(entity.getPrimaryCategoryId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("모든 인자가 null 이어도 기존 값이 전부 유지된다")
        void preservesAllWhenAllNull() {
            ProductCatalogReadModelJpaEntity entity = newEntity();

            entity.updateProductInfo(null, null, null);

            assertThat(entity.getName()).isEqualTo("상품A");
            assertThat(entity.getBrandName()).isEqualTo("브랜드A");
            assertThat(entity.getPrimaryCategoryId()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("updatePricing")
    class UpdatePricing {

        @Test
        @DisplayName("price 등 pricing 필드가 null 로 들어와도 기존 값이 유지된다")
        void preservesPricingWhenNull() {
            ProductCatalogReadModelJpaEntity entity = newEntity();

            entity.updatePricing(null, null, null, null, null);

            assertThat(entity.getPrice()).isEqualTo(10000L);
            assertThat(entity.getDiscountPrice()).isEqualTo(9000L);
            assertThat(entity.getDiscountRate()).isEqualByComparingTo(BigDecimal.valueOf(10.0));
            assertThat(entity.getAccumulatedPoint()).isEqualTo(100L);
            assertThat(entity.getAccumulatedPointRate()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
        }

        @Test
        @DisplayName("일부 필드만 값이 들어오면 해당 필드만 업데이트되고 나머지는 유지된다")
        void updatesOnlyProvidedFields() {
            ProductCatalogReadModelJpaEntity entity = newEntity();

            entity.updatePricing(12000L, null, null, null, null);

            assertThat(entity.getPrice()).isEqualTo(12000L);
            assertThat(entity.getDiscountPrice()).isEqualTo(9000L);
            assertThat(entity.getDiscountRate()).isEqualByComparingTo(BigDecimal.valueOf(10.0));
            assertThat(entity.getAccumulatedPoint()).isEqualTo(100L);
            assertThat(entity.getAccumulatedPointRate()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
        }
    }

    @Nested
    @DisplayName("updateOrderNum / updatePopularity / updateStockQuantity / updateSales")
    class UpdateScalars {

        @Test
        @DisplayName("orderNum 이 null 이면 기존 orderNum 이 유지된다")
        void preservesOrderNumWhenNull() {
            ProductCatalogReadModelJpaEntity entity = newEntity();

            entity.updateOrderNum(null);

            assertThat(entity.getOrderNum()).isEqualTo(5L);
        }

        @Test
        @DisplayName("popularity 가 null 이면 기존 popularity 가 유지된다")
        void preservesPopularityWhenNull() {
            ProductCatalogReadModelJpaEntity entity = newEntity();

            entity.updatePopularity(null);

            assertThat(entity.getPopularity()).isEqualTo(7L);
        }

        @Test
        @DisplayName("stockQuantity 가 null 이면 기존 stockQuantity 가 유지된다")
        void preservesStockQuantityWhenNull() {
            ProductCatalogReadModelJpaEntity entity = newEntity();
            entity.updateStockQuantity(77);

            entity.updateStockQuantity(null);

            assertThat(entity.getStockQuantity()).isEqualTo(77);
        }

        @Test
        @DisplayName("sales 가 null 이면 기존 sales 가 유지된다")
        void preservesSalesWhenNull() {
            ProductCatalogReadModelJpaEntity entity = newEntity();
            entity.updateSales(42L);

            entity.updateSales(null);

            assertThat(entity.getSales()).isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("updateRepresentativeImage / updateReviewAggregate")
    class UpdateRepresentativeAndReview {

        @Test
        @DisplayName("representativeImageUrl 이 null 이면 기존 값이 유지된다")
        void preservesRepresentativeImageWhenNull() {
            ProductCatalogReadModelJpaEntity entity = newEntity();
            entity.updateRepresentativeImage("https://img.example/a.png", "[\"sm\"]");

            entity.updateRepresentativeImage(null, null);

            assertThat(entity.getRepresentativeImageUrl()).isEqualTo("https://img.example/a.png");
            assertThat(entity.getResizedImageUrls()).isEqualTo("[\"sm\"]");
        }

        @Test
        @DisplayName("reviewCount 가 null 이면 기존 집계가 유지된다")
        void preservesReviewAggregateWhenReviewCountNull() {
            ProductCatalogReadModelJpaEntity entity = newEntity();
            entity.updateReviewAggregate(12, new BigDecimal("4.50"));

            entity.updateReviewAggregate(null, new BigDecimal("3.00"));

            assertThat(entity.getReviewCount()).isEqualTo(12);
            assertThat(entity.getReviewAverageRating()).isEqualByComparingTo(new BigDecimal("4.50"));
        }

        @Test
        @DisplayName("reviewAverageRating 이 null 이면 기존 집계가 유지된다")
        void preservesReviewAggregateWhenAverageRatingNull() {
            ProductCatalogReadModelJpaEntity entity = newEntity();
            entity.updateReviewAggregate(12, new BigDecimal("4.50"));

            entity.updateReviewAggregate(20, null);

            assertThat(entity.getReviewCount()).isEqualTo(12);
            assertThat(entity.getReviewAverageRating()).isEqualByComparingTo(new BigDecimal("4.50"));
        }
    }

    @Nested
    @DisplayName("increaseSales")
    class IncreaseSales {

        @Test
        @DisplayName("delta 가 null 이면 0 을 더한 것과 동일하게 기존 sales 가 유지된다")
        void treatsNullDeltaAsZero() {
            ProductCatalogReadModelJpaEntity entity = newEntity();
            entity.updateSales(10L);

            entity.increaseSales(null);

            assertThat(entity.getSales()).isEqualTo(10L);
        }

        @Test
        @DisplayName("delta 가 양수이면 기존 sales 에 누적한다")
        void accumulatesSales() {
            ProductCatalogReadModelJpaEntity entity = newEntity();
            entity.updateSales(10L);

            entity.increaseSales(3L);

            assertThat(entity.getSales()).isEqualTo(13L);
        }
    }
}
