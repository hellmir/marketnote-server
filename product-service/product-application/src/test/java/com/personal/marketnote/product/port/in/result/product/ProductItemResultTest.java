package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.Rate;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.domain.product.ProductTagSnapshotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductItemResultTest {

    @Test
    @DisplayName("상품 태그가 3개 이상이면 orderNum 오름차순 상위 2개만 반환한다")
    void shouldReturnTopTwoTagsOrderedByOrderNumWhenMoreThanTwo() {
        // given
        ProductTag tag1 = createTag(1L, "태그A", 3L);
        ProductTag tag2 = createTag(2L, "태그B", 1L);
        ProductTag tag3 = createTag(3L, "태그C", 2L);
        Product product = createProduct(List.of(tag1, tag2, tag3));

        // when
        ProductItemResult result = ProductItemResult.from(product);

        // then
        assertThat(result.getProductTags()).hasSize(2);
        assertThat(result.getProductTags().get(0).getName()).isEqualTo("태그B");
        assertThat(result.getProductTags().get(1).getName()).isEqualTo("태그C");
    }

    @Test
    @DisplayName("상품 태그가 2개이면 orderNum 오름차순으로 정렬하여 2개 모두 반환한다")
    void shouldReturnTwoTagsSortedWhenExactlyTwo() {
        // given
        ProductTag tag1 = createTag(1L, "태그A", 2L);
        ProductTag tag2 = createTag(2L, "태그B", 1L);
        Product product = createProduct(List.of(tag1, tag2));

        // when
        ProductItemResult result = ProductItemResult.from(product);

        // then
        assertThat(result.getProductTags()).hasSize(2);
        assertThat(result.getProductTags().get(0).getName()).isEqualTo("태그B");
        assertThat(result.getProductTags().get(1).getName()).isEqualTo("태그A");
    }

    @Test
    @DisplayName("상품 태그가 1개이면 그대로 1개만 반환한다")
    void shouldReturnOneTagWhenOnlyOne() {
        // given
        ProductTag tag1 = createTag(1L, "태그A", 1L);
        Product product = createProduct(List.of(tag1));

        // when
        ProductItemResult result = ProductItemResult.from(product);

        // then
        assertThat(result.getProductTags()).hasSize(1);
        assertThat(result.getProductTags().get(0).getName()).isEqualTo("태그A");
    }

    @Test
    @DisplayName("상품 태그가 없으면 빈 리스트를 반환한다")
    void shouldReturnEmptyListWhenNoTags() {
        // given
        Product product = createProduct(List.of());

        // when
        ProductItemResult result = ProductItemResult.from(product);

        // then
        assertThat(result.getProductTags()).isEmpty();
    }

    @Test
    @DisplayName("ReadModel 카탈로그 항목으로부터 상품 항목 결과를 생성한다")
    void shouldCreateFromReadModelCatalogItem() {
        // given
        ProductCatalogItemResult catalogItem = new ProductCatalogItemResult(
                100L,
                10L,
                1L,
                7L,
                "테스트 상품",
                "테스트 브랜드",
                10_000L,
                9_000L,
                BigDecimal.valueOf(10.0),
                100L,
                BigDecimal.ONE,
                5L,
                3L,
                "https://cdn.example/main.jpg",
                List.of("https://cdn.example/sm.jpg", "https://cdn.example/md.jpg"),
                50,
                12,
                4.5f,
                EntityStatus.ACTIVE.name()
        );

        // when
        ProductItemResult result = ProductItemResult.fromReadModel(catalogItem);

        // then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getSellerId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("테스트 상품");
        assertThat(result.getBrandName()).isEqualTo("테스트 브랜드");
        assertThat(result.getPricePolicy().id()).isEqualTo(100L);
        assertThat(result.getPricePolicy().discountPrice()).isEqualTo(9_000L);
        assertThat(result.getCatalogImage()).isNotNull();
        assertThat(result.getCatalogImage().storageUrl()).isEqualTo("https://cdn.example/main.jpg");
        assertThat(result.getCatalogImage().resizedStorageUrls())
                .containsExactly("https://cdn.example/sm.jpg", "https://cdn.example/md.jpg");
        assertThat(result.getStock()).isEqualTo(50);
        assertThat(result.getAverageRating()).isEqualTo(4.5f);
        assertThat(result.getTotalCount()).isEqualTo(12);
        assertThat(result.getOrderNum()).isEqualTo(5L);
        assertThat(result.getStatus()).isEqualTo(EntityStatus.ACTIVE.name());
        assertThat(result.getProductTags()).isEmpty();
    }

    @Test
    @DisplayName("ReadModel 대표 이미지 URL이 없으면 카탈로그 이미지를 null 로 반환한다")
    void shouldReturnNullCatalogImageWhenRepresentativeImageUrlAbsent() {
        // given
        ProductCatalogItemResult catalogItem = new ProductCatalogItemResult(
                200L, 20L, 2L, null, "이미지없음", null,
                5_000L, 5_000L, BigDecimal.ZERO, 0L, BigDecimal.ZERO, 0L, 0L,
                null, List.of(), 0, 0, 0f, EntityStatus.ACTIVE.name()
        );

        // when
        ProductItemResult result = ProductItemResult.fromReadModel(catalogItem);

        // then
        assertThat(result.getCatalogImage()).isNull();
        assertThat(result.getStock()).isZero();
        assertThat(result.getAverageRating()).isZero();
        assertThat(result.getTotalCount()).isZero();
    }

    @Test
    @DisplayName("ReadModel resized 이미지 목록이 null 이면 빈 리스트로 반환한다")
    void shouldReturnEmptyResizedUrlsWhenNull() {
        // given
        ProductCatalogItemResult catalogItem = new ProductCatalogItemResult(
                300L, 30L, 3L, 9L, "리사이즈 없음", "브랜드",
                1_000L, 900L, BigDecimal.valueOf(10.0), 10L, BigDecimal.ONE, 1L, 1L,
                "https://cdn.example/main.jpg", null, 1, 1, 3.0f, EntityStatus.ACTIVE.name()
        );

        // when
        ProductItemResult result = ProductItemResult.fromReadModel(catalogItem);

        // then
        assertThat(result.getCatalogImage().resizedStorageUrls()).isEmpty();
    }

    private ProductTag createTag(Long id, String name, Long orderNum) {
        return ProductTag.from(
                ProductTagSnapshotState.builder()
                        .id(id)
                        .productId(1L)
                        .name(name)
                        .orderNum(orderNum)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private Product createProduct(List<ProductTag> tags) {
        PricePolicy pricePolicy = PricePolicy.from(
                PricePolicySnapshotState.builder()
                        .id(1L)
                        .price(10000L)
                        .discountPrice(9000L)
                        .discountRate(Rate.of(BigDecimal.TEN))
                        .accumulatedPoint(100L)
                        .accumulationRate(Rate.of(BigDecimal.ONE))
                        .orderNum(1L)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );

        return Product.from(
                ProductSnapshotState.builder()
                        .id(1L)
                        .sellerId(1L)
                        .name("테스트 상품")
                        .brandName("테스트 브랜드")
                        .defaultPricePolicy(pricePolicy)
                        .sales(0)
                        .productTags(tags)
                        .orderNum(1L)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
