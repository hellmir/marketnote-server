package com.personal.marketnote.product.domain.option;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductOptionCategory 테스트")
class ProductOptionCategoryTest {

    private Product createProduct() {
        return Product.from(ProductSnapshotState.builder()
                .id(1L)
                .sellerId(1L)
                .name("테스트 상품")
                .status(EntityStatus.ACTIVE)
                .build());
    }

    @Nested
    @DisplayName("from(CreateState)")
    class FromCreateState {

        @Test
        @DisplayName("optionStates가 있으면 옵션을 매핑하여 ACTIVE 상태로 생성된다")
        void shouldMapOptionsAndCreateWithActiveStatus() {
            // given
            Product product = createProduct();
            List<ProductOptionCreateState> optionStates = List.of(
                    ProductOptionCreateState.builder()
                            .category(null)
                            .content("빨강")
                            .build(),
                    ProductOptionCreateState.builder()
                            .category(null)
                            .content("파랑")
                            .build()
            );
            ProductOptionCategoryCreateState state = ProductOptionCategoryCreateState.builder()
                    .product(product)
                    .name("색상")
                    .optionStates(optionStates)
                    .build();

            // when
            ProductOptionCategory category = ProductOptionCategory.from(state);

            // then
            assertThat(category.getProduct()).isEqualTo(product);
            assertThat(category.getName()).isEqualTo("색상");
            assertThat(category.getStatus()).isEqualTo(EntityStatus.ACTIVE);
            assertThat(category.getOptions()).hasSize(2);
        }

        @Test
        @DisplayName("optionStates가 null이면 빈 리스트로 생성된다")
        void shouldCreateWithEmptyListWhenOptionStatesIsNull() {
            // given
            Product product = createProduct();
            ProductOptionCategoryCreateState state = ProductOptionCategoryCreateState.builder()
                    .product(product)
                    .name("색상")
                    .optionStates(null)
                    .build();

            // when
            ProductOptionCategory category = ProductOptionCategory.from(state);

            // then
            assertThat(category.getOptions()).isEmpty();
            assertThat(category.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("optionStates가 빈 리스트이면 빈 리스트로 생성된다")
        void shouldCreateWithEmptyListWhenOptionStatesIsEmpty() {
            // given
            Product product = createProduct();
            ProductOptionCategoryCreateState state = ProductOptionCategoryCreateState.builder()
                    .product(product)
                    .name("색상")
                    .optionStates(List.of())
                    .build();

            // when
            ProductOptionCategory category = ProductOptionCategory.from(state);

            // then
            assertThat(category.getOptions()).isEmpty();
        }
    }

    @Nested
    @DisplayName("from(SnapshotState)")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState의 모든 필드가 그대로 복원된다")
        void shouldRestoreAllFieldsFromSnapshotState() {
            // given
            Product product = createProduct();
            List<ProductOption> options = List.of();
            ProductOptionCategorySnapshotState state = ProductOptionCategorySnapshotState.builder()
                    .id(10L)
                    .product(product)
                    .name("사이즈")
                    .options(options)
                    .orderNum(3L)
                    .status(EntityStatus.INACTIVE)
                    .build();

            // when
            ProductOptionCategory category = ProductOptionCategory.from(state);

            // then
            assertThat(category.getId()).isEqualTo(10L);
            assertThat(category.getProduct()).isEqualTo(product);
            assertThat(category.getName()).isEqualTo("사이즈");
            assertThat(category.getOptions()).isEqualTo(options);
            assertThat(category.getOrderNum()).isEqualTo(3L);
            assertThat(category.getStatus()).isEqualTo(EntityStatus.INACTIVE);
        }
    }
}
