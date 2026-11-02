package com.personal.marketnote.product.adapter.out.persistence.productcatalog.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.configuration.QuerydslConfig;
import com.personal.marketnote.product.adapter.out.persistence.productcatalog.entity.ProductCatalogReadModelJpaEntity;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        AuditConfig.class,
        QuerydslConfig.class,
        ProductCatalogReadModelQueryRepositoryImpl.class
})
class ProductCatalogReadModelQueryRepositoryImplTest {

    @Autowired
    private ProductCatalogReadModelJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    private ProductCatalogReadModelJpaEntity buildEntity(
            Long pricePolicyId,
            Long productId,
            Long primaryCategoryId,
            String name,
            String brandName,
            Long discountPrice,
            Long orderNum,
            Long popularity,
            Long accumulatedPoint,
            BigDecimal accumulatedPointRate
    ) {
        return ProductCatalogReadModelJpaEntity.of(
                pricePolicyId,
                productId,
                200L,
                primaryCategoryId,
                name,
                brandName,
                discountPrice + 1000L,
                discountPrice,
                BigDecimal.valueOf(10.0),
                accumulatedPoint,
                accumulatedPointRate,
                orderNum,
                popularity,
                0L,
                null,
                null
        );
    }

    @Nested
    @DisplayName("findCatalogItems - sortProperty × direction")
    class SortByProperty {

        @Test
        @DisplayName("ORDER_NUM ASC 정렬: orderNum 오름차순으로 반환한다")
        void orderNumAsc() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 9000L, 30L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "B", null, 9000L, 10L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, null, "C", null, 9000L, 20L, 0L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, null, 10, true, ProductSortProperty.ORDER_NUM, null, null
            );

            assertThat(results).extracting(ProductCatalogReadModelJpaEntity::getOrderNum)
                    .containsExactly(10L, 20L, 30L);
        }

        @Test
        @DisplayName("ORDER_NUM DESC 정렬: orderNum 내림차순으로 반환한다")
        void orderNumDesc() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 9000L, 30L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "B", null, 9000L, 10L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, null, "C", null, 9000L, 20L, 0L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, null, 10, false, ProductSortProperty.ORDER_NUM, null, null
            );

            assertThat(results).extracting(ProductCatalogReadModelJpaEntity::getOrderNum)
                    .containsExactly(30L, 20L, 10L);
        }

        @Test
        @DisplayName("POPULARITY ASC 정렬: popularity 오름차순으로 반환한다")
        void popularityAsc() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 9000L, 0L, 50L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "B", null, 9000L, 0L, 10L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, null, "C", null, 9000L, 0L, 80L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, null, 10, true, ProductSortProperty.POPULARITY, null, null
            );

            assertThat(results).extracting(ProductCatalogReadModelJpaEntity::getPopularity)
                    .containsExactly(10L, 50L, 80L);
        }

        @Test
        @DisplayName("POPULARITY DESC 정렬: popularity 내림차순으로 반환한다")
        void popularityDesc() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 9000L, 0L, 50L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "B", null, 9000L, 0L, 10L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, null, "C", null, 9000L, 0L, 80L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, null, 10, false, ProductSortProperty.POPULARITY, null, null
            );

            assertThat(results).extracting(ProductCatalogReadModelJpaEntity::getPopularity)
                    .containsExactly(80L, 50L, 10L);
        }

        @Test
        @DisplayName("ACCUMULATED_POINT ASC 정렬: accumulatedPoint 오름차순으로 반환한다")
        void accumulatedPointAsc() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 9000L, 0L, 0L, 300L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "B", null, 9000L, 0L, 0L, 100L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, null, "C", null, 9000L, 0L, 0L, 200L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, null, 10, true, ProductSortProperty.ACCUMULATED_POINT, null, null
            );

            assertThat(results).extracting(ProductCatalogReadModelJpaEntity::getAccumulatedPoint)
                    .containsExactly(100L, 200L, 300L);
        }

        @Test
        @DisplayName("ACCUMULATED_POINT DESC 정렬: accumulatedPoint 내림차순으로 반환한다")
        void accumulatedPointDesc() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 9000L, 0L, 0L, 300L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "B", null, 9000L, 0L, 0L, 100L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, null, "C", null, 9000L, 0L, 0L, 200L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, null, 10, false, ProductSortProperty.ACCUMULATED_POINT, null, null
            );

            assertThat(results).extracting(ProductCatalogReadModelJpaEntity::getAccumulatedPoint)
                    .containsExactly(300L, 200L, 100L);
        }

        @Test
        @DisplayName("ACCUMULATED_POINT_RATE ASC 정렬: accumulatedPointRate 오름차순으로 반환한다")
        void accumulatedPointRateAsc() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 9000L, 0L, 0L, 0L, BigDecimal.valueOf(3.0)));
            repository.save(buildEntity(2L, 101L, null, "B", null, 9000L, 0L, 0L, 0L, BigDecimal.valueOf(1.0)));
            repository.save(buildEntity(3L, 102L, null, "C", null, 9000L, 0L, 0L, 0L, BigDecimal.valueOf(2.0)));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, null, 10, true, ProductSortProperty.ACCUMULATED_POINT_RATE, null, null
            );

            assertThat(results).extracting(r -> r.getAccumulatedPointRate().doubleValue())
                    .containsExactly(1.0, 2.0, 3.0);
        }

        @Test
        @DisplayName("ACCUMULATED_POINT_RATE DESC 정렬: accumulatedPointRate 내림차순으로 반환한다")
        void accumulatedPointRateDesc() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 9000L, 0L, 0L, 0L, BigDecimal.valueOf(3.0)));
            repository.save(buildEntity(2L, 101L, null, "B", null, 9000L, 0L, 0L, 0L, BigDecimal.valueOf(1.0)));
            repository.save(buildEntity(3L, 102L, null, "C", null, 9000L, 0L, 0L, 0L, BigDecimal.valueOf(2.0)));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, null, 10, false, ProductSortProperty.ACCUMULATED_POINT_RATE, null, null
            );

            assertThat(results).extracting(r -> r.getAccumulatedPointRate().doubleValue())
                    .containsExactly(3.0, 2.0, 1.0);
        }

        @Test
        @DisplayName("DISCOUNT_PRICE ASC 정렬: discountPrice 오름차순으로 반환한다")
        void discountPriceAsc() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "B", null, 7000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, null, "C", null, 12000L, 0L, 0L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, null, 10, true, ProductSortProperty.DISCOUNT_PRICE, null, null
            );

            assertThat(results).extracting(ProductCatalogReadModelJpaEntity::getDiscountPrice)
                    .containsExactly(7000L, 9000L, 12000L);
        }

        @Test
        @DisplayName("DISCOUNT_PRICE DESC 정렬: discountPrice 내림차순으로 반환한다")
        void discountPriceDesc() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "B", null, 7000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, null, "C", null, 12000L, 0L, 0L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, null, 10, false, ProductSortProperty.DISCOUNT_PRICE, null, null
            );

            assertThat(results).extracting(ProductCatalogReadModelJpaEntity::getDiscountPrice)
                    .containsExactly(12000L, 9000L, 7000L);
        }
    }

    @Nested
    @DisplayName("findCatalogItems - searchTarget × keyword")
    class SearchByTarget {

        @Test
        @DisplayName("searchTarget=NAME 이면 name LIKE 검색으로 필터링한다")
        void searchByName() {
            repository.save(buildEntity(1L, 100L, null, "사과주스", "브랜드A", 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "바나나우유", "브랜드B", 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, null, "사과파이", "브랜드C", 9000L, 0L, 0L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, null, 10, true, ProductSortProperty.ORDER_NUM,
                    ProductSearchTarget.NAME, "사과"
            );

            assertThat(results).extracting(ProductCatalogReadModelJpaEntity::getName)
                    .containsExactlyInAnyOrder("사과주스", "사과파이");
        }

        @Test
        @DisplayName("searchTarget=BRAND_NAME 이면 brandName LIKE 검색으로 필터링한다")
        void searchByBrandName() {
            repository.save(buildEntity(1L, 100L, null, "상품1", "나이키", 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "상품2", "아디다스", 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, null, "상품3", "나이키스포츠", 9000L, 0L, 0L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, null, 10, true, ProductSortProperty.ORDER_NUM,
                    ProductSearchTarget.BRAND_NAME, "나이키"
            );

            assertThat(results).extracting(ProductCatalogReadModelJpaEntity::getBrandName)
                    .containsExactlyInAnyOrder("나이키", "나이키스포츠");
        }
    }

    @Nested
    @DisplayName("findCatalogItems - cursor pagination")
    class CursorPagination {

        @Test
        @DisplayName("유효 커서: 커서 이후 레코드만 반환한다")
        void returnsAfterCursor() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 7000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "B", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, null, "C", null, 12000L, 0L, 0L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, 2L, 10, true, ProductSortProperty.DISCOUNT_PRICE, null, null
            );

            assertThat(results).extracting(ProductCatalogReadModelJpaEntity::getPricePolicyId)
                    .containsExactly(3L);
        }

        @Test
        @DisplayName("삭제된 커서(미존재 pricePolicyId): 빈 리스트를 반환한다")
        void returnsEmptyWhenCursorMissing() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 7000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "B", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, 999L, 10, true, ProductSortProperty.DISCOUNT_PRICE, null, null
            );

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("커서 null 또는 0 이하: 1페이지 전체 반환한다")
        void returnsFirstPageWhenCursorAbsent() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 7000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "B", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> nullCursor = repository.findCatalogItems(
                    null, null, null, 10, true, ProductSortProperty.DISCOUNT_PRICE, null, null
            );
            List<ProductCatalogReadModelJpaEntity> zeroCursor = repository.findCatalogItems(
                    null, null, 0L, 10, true, ProductSortProperty.DISCOUNT_PRICE, null, null
            );

            assertThat(nullCursor).hasSize(2);
            assertThat(zeroCursor).hasSize(2);
        }

        @Test
        @DisplayName("pageSize 제한: 지정된 수만큼만 반환한다")
        void respectsPageSize() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 7000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "B", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, null, "C", null, 12000L, 0L, 0L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, null, 2, true, ProductSortProperty.DISCOUNT_PRICE, null, null
            );

            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findCatalogItems - filters")
    class Filters {

        @Test
        @DisplayName("categoryId 필터: primaryCategoryId 일치 레코드만 반환한다")
        void filtersByCategoryId() {
            repository.save(buildEntity(1L, 100L, 10L, "A", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, 20L, "B", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, 10L, "C", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    10L, null, null, 10, true, ProductSortProperty.ORDER_NUM, null, null
            );

            assertThat(results).extracting(ProductCatalogReadModelJpaEntity::getPricePolicyId)
                    .containsExactlyInAnyOrder(1L, 3L);
        }

        @Test
        @DisplayName("pricePolicyIds 필터: IN 조건 레코드만 반환한다")
        void filtersByPricePolicyIds() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "B", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, null, "C", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, List.of(1L, 3L), null, 10, true, ProductSortProperty.ORDER_NUM, null, null
            );

            assertThat(results).extracting(ProductCatalogReadModelJpaEntity::getPricePolicyId)
                    .containsExactlyInAnyOrder(1L, 3L);
        }

        @Test
        @DisplayName("INACTIVE 레코드는 조회 결과에서 제외한다")
        void excludesInactive() {
            ProductCatalogReadModelJpaEntity active = buildEntity(1L, 100L, null, "A", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO);
            ProductCatalogReadModelJpaEntity inactive = buildEntity(2L, 101L, null, "B", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO);
            inactive.markInactive();
            repository.save(active);
            repository.save(inactive);

            List<ProductCatalogReadModelJpaEntity> results = repository.findCatalogItems(
                    null, null, null, 10, true, ProductSortProperty.ORDER_NUM, null, null
            );

            assertThat(results).extracting(ProductCatalogReadModelJpaEntity::getPricePolicyId)
                    .containsExactly(1L);
        }
    }

    @Nested
    @DisplayName("countCatalogItems")
    class CountCatalogItems {

        @Test
        @DisplayName("필터 없을 때: ACTIVE 전체 건수를 반환한다")
        void countsAllActive() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "B", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));

            long count = repository.countCatalogItems(null, null, null);

            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("INACTIVE 레코드는 count에서 제외한다")
        void excludesInactiveFromCount() {
            repository.save(buildEntity(1L, 100L, null, "A", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            ProductCatalogReadModelJpaEntity inactive = buildEntity(2L, 101L, null, "B", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO);
            inactive.markInactive();
            repository.save(inactive);

            long count = repository.countCatalogItems(null, null, null);

            assertThat(count).isEqualTo(1L);
        }

        @Test
        @DisplayName("categoryId 필터: primaryCategoryId 일치 건수를 반환한다")
        void countsByCategoryId() {
            repository.save(buildEntity(1L, 100L, 10L, "A", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, 20L, "B", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, 10L, "C", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));

            long count = repository.countCatalogItems(10L, null, null);

            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("검색어 필터: name LIKE 일치 건수를 반환한다")
        void countsBySearchKeyword() {
            repository.save(buildEntity(1L, 100L, null, "사과주스", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, null, "바나나우유", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, null, "사과파이", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));

            long count = repository.countCatalogItems(null, ProductSearchTarget.NAME, "사과");

            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("findCatalogItems 결과 건수와 countCatalogItems 값이 일치한다 (동등성)")
        void countsMatchFindResults() {
            repository.save(buildEntity(1L, 100L, 10L, "사과주스", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(2L, 101L, 10L, "사과파이", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));
            repository.save(buildEntity(3L, 102L, 20L, "바나나", null, 9000L, 0L, 0L, 0L, BigDecimal.ZERO));

            List<ProductCatalogReadModelJpaEntity> findResults = repository.findCatalogItems(
                    10L, null, null, 100, true, ProductSortProperty.ORDER_NUM,
                    ProductSearchTarget.NAME, "사과"
            );
            long count = repository.countCatalogItems(10L, ProductSearchTarget.NAME, "사과");

            assertThat(findResults).hasSize((int) count);
            assertThat(count).isEqualTo(2L);
        }
    }
}
