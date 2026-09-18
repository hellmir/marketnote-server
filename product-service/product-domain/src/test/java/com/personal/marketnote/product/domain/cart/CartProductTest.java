package com.personal.marketnote.product.domain.cart;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CartProduct 테스트")
class CartProductTest {

    private PricePolicy createPricePolicy() {
        return PricePolicy.from(PricePolicySnapshotState.builder()
                .id(1L)
                .productId(1L)
                .price(10000L)
                .status(EntityStatus.ACTIVE)
                .build());
    }

    @Nested
    @DisplayName("from(CreateState)")
    class FromCreateState {

        @Test
        @DisplayName("CreateState로 생성하면 ACTIVE 상태로 생성된다")
        void shouldCreateWithActiveStatus() {
            // given
            CartProductCreateState state = CartProductCreateState.builder()
                    .userId(1L)
                    .sharerKey(UUID.randomUUID())
                    .pricePolicy(createPricePolicy())
                    .imageUrl("https://example.com/image.jpg")
                    .quantity((short) 2)
                    .build();

            // when
            CartProduct cartProduct = CartProduct.from(state);

            // then
            assertThat(cartProduct.getUserId()).isEqualTo(1L);
            assertThat(cartProduct.getQuantity()).isEqualTo((short) 2);
            assertThat(cartProduct.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("from(SnapshotState)")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState의 모든 필드가 그대로 복원된다")
        void shouldRestoreAllFieldsFromSnapshotState() {
            // given
            UUID sharerKey = UUID.randomUUID();
            PricePolicy pricePolicy = createPricePolicy();
            CartProductSnapshotState state = CartProductSnapshotState.builder()
                    .userId(1L)
                    .sharerKey(sharerKey)
                    .pricePolicy(pricePolicy)
                    .imageUrl("https://example.com/image.jpg")
                    .quantity((short) 3)
                    .status(EntityStatus.INACTIVE)
                    .build();

            // when
            CartProduct cartProduct = CartProduct.from(state);

            // then
            assertThat(cartProduct.getUserId()).isEqualTo(1L);
            assertThat(cartProduct.getSharerKey()).isEqualTo(sharerKey);
            assertThat(cartProduct.getQuantity()).isEqualTo((short) 3);
            assertThat(cartProduct.isInactive()).isTrue();
        }
    }

    @Nested
    @DisplayName("addQuantity()")
    class AddQuantity {

        @Test
        @DisplayName("유효한 수량을 추가하면 수량이 증가한다")
        void shouldIncreaseQuantityWhenValidAdditionalQuantity() {
            // given
            CartProduct cartProduct = CartProduct.from(CartProductCreateState.builder()
                    .userId(1L)
                    .sharerKey(UUID.randomUUID())
                    .pricePolicy(createPricePolicy())
                    .imageUrl("https://example.com/image.jpg")
                    .quantity((short) 5)
                    .build());

            // when
            cartProduct.addQuantity((short) 3);

            // then
            assertThat(cartProduct.getQuantity()).isEqualTo((short) 8);
        }

        @Test
        @DisplayName("0 이하의 수량을 추가하면 InvalidCartProductQuantityException이 발생한다")
        void shouldThrowExceptionWhenAdditionalQuantityIsZeroOrNegative() {
            // given
            CartProduct cartProduct = CartProduct.from(CartProductCreateState.builder()
                    .userId(1L)
                    .sharerKey(UUID.randomUUID())
                    .pricePolicy(createPricePolicy())
                    .imageUrl("https://example.com/image.jpg")
                    .quantity((short) 5)
                    .build());

            // when & then
            assertThatThrownBy(() -> cartProduct.addQuantity((short) 0))
                    .isInstanceOf(InvalidCartProductQuantityException.class);
        }

        @Test
        @DisplayName("Short.MAX_VALUE를 초과하면 InvalidCartProductQuantityException이 발생한다")
        void shouldThrowExceptionWhenQuantityExceedsShortMaxValue() {
            // given
            CartProduct cartProduct = CartProduct.from(CartProductCreateState.builder()
                    .userId(1L)
                    .sharerKey(UUID.randomUUID())
                    .pricePolicy(createPricePolicy())
                    .imageUrl("https://example.com/image.jpg")
                    .quantity(Short.MAX_VALUE)
                    .build());

            // when & then
            assertThatThrownBy(() -> cartProduct.addQuantity((short) 1))
                    .isInstanceOf(InvalidCartProductQuantityException.class);
        }
    }
}
