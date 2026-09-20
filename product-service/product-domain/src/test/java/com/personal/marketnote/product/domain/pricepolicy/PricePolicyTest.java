package com.personal.marketnote.product.domain.pricepolicy;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.money.Money;
import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.option.ProductOptionSnapshotState;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PricePolicyTest {

    @Test
    @DisplayName("CreateState에 product가 있으면 productId가 설정된다")
    void shouldSetProductIdWhenProductExistsInCreateState() {
        Product product = createProduct(1L);
        PricePolicyCreateState state = PricePolicyCreateState.builder()
                .product(product)
                .price(10000L)
                .discountPrice(8000L)
                .discountRate(Rate.of(BigDecimal.valueOf(20)))
                .accumulatedPoint(100L)
                .accumulationRate(Rate.of(BigDecimal.valueOf(1)))
                .popularity(0L)
                .orderNum(1L)
                .optionIds(List.of(1L, 2L))
                .build();

        PricePolicy pricePolicy = PricePolicy.from(state);

        assertThat(pricePolicy.getProductId()).isEqualTo(1L);
        assertThat(pricePolicy.getPrice()).isEqualTo(Money.of(10000L));
        assertThat(pricePolicy.getDiscountPrice()).isEqualTo(Money.of(8000L));
    }

    @Test
    @DisplayName("CreateState에 product가 null이면 productId도 null이다")
    void shouldSetProductIdNullWhenProductIsNullInCreateState() {
        PricePolicyCreateState state = PricePolicyCreateState.builder()
                .product(null)
                .price(10000L)
                .build();

        PricePolicy pricePolicy = PricePolicy.from(state);

        assertThat(pricePolicy.getProductId()).isNull();
    }

    @Test
    @DisplayName("SnapshotState로 복원하면 모든 필드가 올바르게 매핑된다")
    void shouldRestoreAllFieldsFromSnapshotState() {
        PricePolicySnapshotState state = PricePolicySnapshotState.builder()
                .id(10L)
                .productId(1L)
                .price(15000L)
                .discountPrice(12000L)
                .discountRate(Rate.of(BigDecimal.valueOf(20)))
                .accumulatedPoint(150L)
                .accumulationRate(Rate.of(BigDecimal.valueOf(1)))
                .popularity(50L)
                .orderNum(3L)
                .status(EntityStatus.ACTIVE)
                .optionIds(List.of(1L, 2L))
                .build();

        PricePolicy pricePolicy = PricePolicy.from(state);

        assertThat(pricePolicy.getId()).isEqualTo(10L);
        assertThat(pricePolicy.getProductId()).isEqualTo(1L);
        assertThat(pricePolicy.getPrice()).isEqualTo(Money.of(15000L));
        assertThat(pricePolicy.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(pricePolicy.getOptionIds()).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("SnapshotState에서 productId가 null이고 product가 있으면 product.getId()로 설정된다")
    void shouldDeriveProductIdFromProductWhenProductIdIsNull() {
        Product product = createProduct(5L);
        PricePolicySnapshotState state = PricePolicySnapshotState.builder()
                .id(10L)
                .productId(null)
                .product(product)
                .price(10000L)
                .status(EntityStatus.ACTIVE)
                .build();

        PricePolicy pricePolicy = PricePolicy.from(state);

        assertThat(pricePolicy.getProductId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("addProduct를 호출하면 product와 productId가 설정된다")
    void shouldSetProductAndProductIdWhenAddProductCalled() {
        PricePolicy pricePolicy = PricePolicy.from(PricePolicyCreateState.builder()
                .product(null)
                .price(10000L)
                .build());

        Product product = createProduct(3L);
        pricePolicy.addProduct(product);

        assertThat(pricePolicy.getProduct()).isEqualTo(product);
        assertThat(pricePolicy.getProductId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("optionIds가 있으면 hasOptions는 true를 반환한다")
    void shouldReturnTrueWhenOptionIdsExist() {
        PricePolicy pricePolicy = PricePolicy.from(PricePolicyCreateState.builder()
                .product(null)
                .price(10000L)
                .optionIds(List.of(1L, 2L))
                .build());

        assertThat(pricePolicy.hasOptions()).isTrue();
    }

    @Test
    @DisplayName("optionIds가 null이면 hasOptions는 false를 반환한다")
    void shouldReturnFalseWhenOptionIdsIsNull() {
        PricePolicy pricePolicy = PricePolicy.from(PricePolicyCreateState.builder()
                .product(null)
                .price(10000L)
                .optionIds(null)
                .build());

        assertThat(pricePolicy.hasOptions()).isFalse();
    }

    @Test
    @DisplayName("optionIds가 빈 리스트이면 hasOptions는 false를 반환한다")
    void shouldReturnFalseWhenOptionIdsIsEmpty() {
        PricePolicy pricePolicy = PricePolicy.from(PricePolicyCreateState.builder()
                .product(null)
                .price(10000L)
                .optionIds(List.of())
                .build());

        assertThat(pricePolicy.hasOptions()).isFalse();
    }

    @Test
    @DisplayName("productId가 있으면 hasProduct는 true를 반환한다")
    void shouldReturnTrueWhenProductIdExists() {
        Product product = createProduct(1L);
        PricePolicy pricePolicy = PricePolicy.from(PricePolicyCreateState.builder()
                .product(product)
                .price(10000L)
                .build());

        assertThat(pricePolicy.hasProduct()).isTrue();
    }

    @Test
    @DisplayName("productId가 null이면 hasProduct는 false를 반환한다")
    void shouldReturnFalseWhenProductIdIsNull() {
        PricePolicy pricePolicy = PricePolicy.from(PricePolicyCreateState.builder()
                .product(null)
                .price(10000L)
                .build());

        assertThat(pricePolicy.hasProduct()).isFalse();
    }

    @Test
    @DisplayName("addProduct 호출 시 이미 productId가 있으면 덮어쓰지 않는다")
    void shouldNotOverwriteExistingProductIdWhenAddProductCalled() {
        Product originalProduct = createProduct(1L);
        PricePolicy pricePolicy = PricePolicy.from(PricePolicyCreateState.builder()
                .product(originalProduct)
                .price(10000L)
                .build());

        Product newProduct = createProduct(99L);
        pricePolicy.addProduct(newProduct);

        assertThat(pricePolicy.getProduct()).isEqualTo(newProduct);
        assertThat(pricePolicy.getProductId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("setOptionIds를 호출하면 productOptions에서 id 목록이 추출된다")
    void shouldExtractOptionIdsFromProductOptions() {
        List<ProductOption> options = List.of(
                createProductOption(10L),
                createProductOption(20L)
        );
        PricePolicySnapshotState state = PricePolicySnapshotState.builder()
                .id(1L)
                .productId(1L)
                .price(10000L)
                .status(EntityStatus.ACTIVE)
                .productOptions(options)
                .build();

        PricePolicy pricePolicy = PricePolicy.from(state);
        pricePolicy.setOptionIds();

        assertThat(pricePolicy.getOptionIds()).containsExactly(10L, 20L);
    }

    private Product createProduct(Long id) {
        return Product.from(ProductSnapshotState.builder()
                .id(id)
                .sellerId(1L)
                .name("테스트 상품")
                .status(EntityStatus.ACTIVE)
                .build());
    }

    private ProductOption createProductOption(Long id) {
        return ProductOption.from(ProductOptionSnapshotState.builder()
                .id(id)
                .content("옵션")
                .status(EntityStatus.ACTIVE)
                .build());
    }
}
