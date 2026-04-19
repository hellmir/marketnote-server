package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
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
                        .discountRate(BigDecimal.TEN)
                        .accumulatedPoint(100L)
                        .accumulationRate(BigDecimal.ONE)
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
