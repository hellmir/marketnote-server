package com.personal.marketnote.product.service.cart;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.domain.cart.CartProductSnapshotState;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.exception.CartProductNotFoundException;
import com.personal.marketnote.product.port.in.usecase.product.GetProductInventoryUseCase;
import com.personal.marketnote.product.port.out.cart.FindCartProductPort;
import com.personal.marketnote.product.port.out.cart.FindCartProductsPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCartProductUseCaseTest {
    @Mock
    private FindCartProductPort findCartProductPort;
    @Mock
    private GetProductInventoryUseCase getProductInventoryUseCase;
    @Mock
    private FindCartProductsPort findCartProductsPort;

    @InjectMocks
    private GetCartProductService getCartProductService;

    @Test
    @DisplayName("장바구니 상품 존재 여부 조회 시 존재하면 true를 반환한다")
    void existsByUserIdAndPolicyId_returnsTrue() {
        when(findCartProductPort.existsByUserIdAndPolicyId(1L, 100L)).thenReturn(true);

        boolean result = getCartProductService.existsByUserIdAndPolicyId(1L, 100L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("장바구니 상품 존재 여부 조회 시 없으면 false를 반환한다")
    void existsByUserIdAndPolicyId_returnsFalse() {
        when(findCartProductPort.existsByUserIdAndPolicyId(2L, 200L)).thenReturn(false);

        boolean result = getCartProductService.existsByUserIdAndPolicyId(2L, 200L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("장바구니 상품 존재 여부 조회 시 포트 예외를 전파한다")
    void existsByUserIdAndPolicyId_portFails_propagates() {
        RuntimeException exception = new RuntimeException("exist fail");
        when(findCartProductPort.existsByUserIdAndPolicyId(3L, 300L)).thenThrow(exception);

        assertThatThrownBy(() -> getCartProductService.existsByUserIdAndPolicyId(3L, 300L))
                .isSameAs(exception);
    }

    @Test
    @DisplayName("장바구니 상품 정보 조회 시 장바구니 상품을 반환한다")
    void getCartProduct_returnsCartProduct() {
        Long userId = 10L;
        Long pricePolicyId = 1000L;
        PricePolicy pricePolicy = buildPricePolicy(pricePolicyId);
        CartProduct cartProduct = buildCartProduct(userId, pricePolicy, "https://image", (short) 2);

        when(findCartProductPort.findCartProductByUserIdAndPricePolicyId(userId, pricePolicyId))
                .thenReturn(Optional.of(cartProduct));

        CartProduct result = getCartProductService.getCartProduct(userId, pricePolicyId);

        assertThat(result).isSameAs(cartProduct);
    }

    @Test
    @DisplayName("장바구니 상품 정보 조회 시 없으면 예외를 던진다")
    void getCartProduct_notFound_throws() {
        Long userId = 11L;
        Long pricePolicyId = 1100L;

        when(findCartProductPort.findCartProductByUserIdAndPricePolicyId(userId, pricePolicyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> getCartProductService.getCartProduct(userId, pricePolicyId))
                .isInstanceOf(CartProductNotFoundException.class)
                .hasMessageContaining("가격 정책 ID: 1100");
    }

    @Test
    @DisplayName("장바구니 상품 정보 조회 시 포트 예외를 전파한다")
    void getCartProduct_portFails_propagates() {
        Long userId = 12L;
        Long pricePolicyId = 1200L;
        RuntimeException exception = new RuntimeException("find fail");

        when(findCartProductPort.findCartProductByUserIdAndPricePolicyId(userId, pricePolicyId))
                .thenThrow(exception);

        assertThatThrownBy(() -> getCartProductService.getCartProduct(userId, pricePolicyId))
                .isSameAs(exception);
    }

    private PricePolicy buildPricePolicy(Long id) {
        return PricePolicy.from(
                PricePolicySnapshotState.builder()
                        .id(id)
                        .price(10000L)
                        .discountPrice(8000L)
                        .discountRate(new BigDecimal("20.0"))
                        .accumulatedPoint(200L)
                        .accumulationRate(new BigDecimal("2.5"))
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private CartProduct buildCartProduct(Long userId, PricePolicy pricePolicy, String imageUrl, Short quantity) {
        return CartProduct.from(
                CartProductSnapshotState.builder()
                        .userId(userId)
                        .pricePolicy(pricePolicy)
                        .imageUrl(imageUrl)
                        .quantity(quantity)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
