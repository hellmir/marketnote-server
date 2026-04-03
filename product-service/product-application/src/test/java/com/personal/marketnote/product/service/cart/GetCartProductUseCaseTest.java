package com.personal.marketnote.product.service.cart;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.domain.cart.CartProductSnapshotState;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.exception.CartProductNotFoundException;
import com.personal.marketnote.product.port.in.result.cart.GetCartProductResult;
import com.personal.marketnote.product.port.in.result.cart.GetMyCartProductsResult;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
        CartProduct cartProduct = buildCartProduct(userId, null, pricePolicy, "https://image", (short) 2);

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

    @Test
    @DisplayName("회원 장바구니 상품 목록 조회 시 장바구니 상품을 매핑해 반환한다")
    void getMyCartProducts_mapsCartProducts() {
        Long userId = 20L;
        Product product1 = buildProduct(1L);
        Product product2 = buildProduct(2L);
        PricePolicy policy1 = buildPricePolicy(100L, product1);
        PricePolicy policy2 = buildPricePolicy(200L, product2);
        UUID sharerKey1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID sharerKey2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        CartProduct cart1 = buildCartProduct(userId, sharerKey1, policy1, "url-1", (short) 2);
        CartProduct cart2 = buildCartProduct(userId, sharerKey2, policy2, "url-2", (short) 1);

        when(findCartProductsPort.findByUserId(userId)).thenReturn(List.of(cart1, cart2));
        when(getProductInventoryUseCase.getProductStocks(List.of(100L, 200L)))
                .thenReturn(Map.of(100L, 5, 200L, 0));

        GetMyCartProductsResult result = getCartProductService.getMyCartProducts(userId);

        assertThat(result.cartProducts()).hasSize(2);
        GetCartProductResult first = result.cartProducts().getFirst();
        assertThat(first.pricePolicy().id()).isEqualTo(100L);
        assertThat(first.product().id()).isEqualTo(1L);
        assertThat(first.product().imageUrl()).isEqualTo("url-1");
        assertThat(first.quantity()).isEqualTo((short) 2);
        assertThat(first.stock()).isEqualTo(5);
        assertThat(first.sharerKey()).isEqualTo(sharerKey1);

        GetCartProductResult second = result.cartProducts().get(1);
        assertThat(second.pricePolicy().id()).isEqualTo(200L);
        assertThat(second.product().id()).isEqualTo(2L);
        assertThat(second.product().imageUrl()).isEqualTo("url-2");
        assertThat(second.quantity()).isEqualTo((short) 1);
        assertThat(second.stock()).isEqualTo(0);
        assertThat(second.sharerKey()).isEqualTo(sharerKey2);

        verify(findCartProductsPort).findByUserId(userId);
        verify(getProductInventoryUseCase).getProductStocks(List.of(100L, 200L));
    }

    @Test
    @DisplayName("회원 장바구니 상품 목록 조회 시 장바구니가 비어 있으면 빈 목록을 반환한다")
    void getMyCartProducts_empty_returnsEmpty() {
        Long userId = 21L;

        when(findCartProductsPort.findByUserId(userId)).thenReturn(List.of());
        when(getProductInventoryUseCase.getProductStocks(List.of())).thenReturn(Map.of());

        GetMyCartProductsResult result = getCartProductService.getMyCartProducts(userId);

        assertThat(result.cartProducts()).isEmpty();
        verify(findCartProductsPort).findByUserId(userId);
        verify(getProductInventoryUseCase).getProductStocks(List.of());
    }

    @Test
    @DisplayName("회원 장바구니 상품 목록 조회 시 재고 정보가 없으면 재고 정보에 null을 반환한다")
    void getMyCartProducts_missingStock_returnsNullStock() {
        Long userId = 22L;
        Product product = buildProduct(3L);
        PricePolicy policy = buildPricePolicy(300L, product);
        CartProduct cartProduct = buildCartProduct(userId, UUID.fromString("550e8400-e29b-41d4-a716-446655440003"), policy, "url-3", (short) 3);

        when(findCartProductsPort.findByUserId(userId)).thenReturn(List.of(cartProduct));
        when(getProductInventoryUseCase.getProductStocks(List.of(300L))).thenReturn(Map.of());

        GetMyCartProductsResult result = getCartProductService.getMyCartProducts(userId);

        assertThat(result.cartProducts()).hasSize(1);
        assertThat(result.cartProducts().getFirst().stock()).isNull();
    }

    @Test
    @DisplayName("회원 장바구니 상품 목록 조회 시 장바구니 조회에 실패하면 예외를 전파한다")
    void getMyCartProducts_findCartProductsFails_propagates() {
        Long userId = 23L;
        RuntimeException exception = new RuntimeException("cart find fail");

        when(findCartProductsPort.findByUserId(userId)).thenThrow(exception);

        assertThatThrownBy(() -> getCartProductService.getMyCartProducts(userId))
                .isSameAs(exception);

        verifyNoInteractions(getProductInventoryUseCase);
    }

    @Test
    @DisplayName("회원 장바구니 상품 목록 조회 시 재고 조회에 실패하면 예외를 전파한다")
    void getMyCartProducts_inventoryFails_propagates() {
        Long userId = 24L;
        Product product = buildProduct(4L);
        PricePolicy policy = buildPricePolicy(400L, product);
        CartProduct cartProduct = buildCartProduct(userId, UUID.fromString("550e8400-e29b-41d4-a716-446655440004"), policy, "url-4", (short) 4);
        RuntimeException exception = new RuntimeException("inventory fail");

        when(findCartProductsPort.findByUserId(userId)).thenReturn(List.of(cartProduct));
        when(getProductInventoryUseCase.getProductStocks(List.of(400L))).thenThrow(exception);

        assertThatThrownBy(() -> getCartProductService.getMyCartProducts(userId))
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

    private PricePolicy buildPricePolicy(Long id, Product product) {
        return PricePolicy.from(
                PricePolicySnapshotState.builder()
                        .id(id)
                        .product(product)
                        .price(10000L)
                        .discountPrice(8000L)
                        .discountRate(new BigDecimal("20.0"))
                        .accumulatedPoint(200L)
                        .accumulationRate(new BigDecimal("2.5"))
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private Product buildProduct(Long id) {
        return Product.from(
                ProductSnapshotState.builder()
                        .id(id)
                        .sellerId(1L)
                        .name("상품-" + id)
                        .brandName("브랜드-" + id)
                        .detail("설명-" + id)
                        .findAllOptionsYn(false)
                        .productTags(List.of())
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private CartProduct buildCartProduct(
            Long userId,
            UUID sharerKey,
            PricePolicy pricePolicy,
            String imageUrl,
            Short quantity
    ) {
        return CartProduct.from(
                CartProductSnapshotState.builder()
                        .userId(userId)
                        .sharerKey(sharerKey)
                        .pricePolicy(pricePolicy)
                        .imageUrl(imageUrl)
                        .quantity(quantity)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
