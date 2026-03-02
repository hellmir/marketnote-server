package com.personal.marketnote.product.service.cart;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.domain.cart.CartProductSnapshotState;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.exception.CartProductNotFoundException;
import com.personal.marketnote.product.port.in.command.UpdateCartProductQuantityCommand;
import com.personal.marketnote.product.port.in.usecase.cart.GetCartProductUseCase;
import com.personal.marketnote.product.port.out.cart.UpdateCartProductPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCartProductQuantityUseCaseTest {
    @Mock
    private GetCartProductUseCase getCartProductUseCase;
    @Mock
    private UpdateCartProductPort updateCartProductPort;

    @InjectMocks
    private UpdateCartProductQuantityService updateCartProductQuantityService;

    @Test
    @DisplayName("장바구니 상품 수량 변경 시 조회 후 수량을 변경하고 저장한다")
    void updateCartProductQuantity_success_updatesAndSaves() {
        // given
        Long userId = 1L;
        Long pricePolicyId = 100L;
        Short originalQuantity = (short) 2;
        Short newQuantity = (short) 5;

        UpdateCartProductQuantityCommand command =
                UpdateCartProductQuantityCommand.of(userId, pricePolicyId, newQuantity);
        CartProduct cartProduct = buildCartProduct(userId, pricePolicyId, originalQuantity);

        when(getCartProductUseCase.getCartProduct(userId, pricePolicyId)).thenReturn(cartProduct);

        // when
        updateCartProductQuantityService.updateCartProductQuantity(command);

        // then
        assertThat(cartProduct.getQuantity()).isEqualTo(newQuantity);

        ArgumentCaptor<CartProduct> captor = ArgumentCaptor.forClass(CartProduct.class);
        verify(updateCartProductPort).update(captor.capture(), eq(pricePolicyId));
        CartProduct saved = captor.getValue();
        assertThat(saved.getQuantity()).isEqualTo(newQuantity);
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getPricePolicyId()).isEqualTo(pricePolicyId);
    }

    @Test
    @DisplayName("장바구니 상품 조회에 실패하면 예외를 전파한다")
    void updateCartProductQuantity_cartProductNotFound_propagatesException() {
        // given
        Long userId = 1L;
        Long pricePolicyId = 999L;
        Short newQuantity = (short) 3;

        UpdateCartProductQuantityCommand command =
                UpdateCartProductQuantityCommand.of(userId, pricePolicyId, newQuantity);
        CartProductNotFoundException exception = new CartProductNotFoundException(pricePolicyId);

        when(getCartProductUseCase.getCartProduct(userId, pricePolicyId)).thenThrow(exception);

        // when & then
        assertThatThrownBy(() -> updateCartProductQuantityService.updateCartProductQuantity(command))
                .isSameAs(exception);

        verifyNoInteractions(updateCartProductPort);
    }

    @Test
    @DisplayName("장바구니 상품 수정 저장에 실패하면 예외를 전파한다")
    void updateCartProductQuantity_updateFails_propagatesException() {
        // given
        Long userId = 1L;
        Long pricePolicyId = 100L;
        Short newQuantity = (short) 5;

        UpdateCartProductQuantityCommand command =
                UpdateCartProductQuantityCommand.of(userId, pricePolicyId, newQuantity);
        CartProduct cartProduct = buildCartProduct(userId, pricePolicyId, (short) 2);
        RuntimeException exception = new RuntimeException("update fail");

        when(getCartProductUseCase.getCartProduct(userId, pricePolicyId)).thenReturn(cartProduct);
        doThrow(exception).when(updateCartProductPort).update(any(CartProduct.class), eq(pricePolicyId));

        // when & then
        assertThatThrownBy(() -> updateCartProductQuantityService.updateCartProductQuantity(command))
                .isSameAs(exception);
    }

    private CartProduct buildCartProduct(Long userId, Long pricePolicyId, Short quantity) {
        PricePolicy pricePolicy = PricePolicy.from(
                PricePolicySnapshotState.builder()
                        .id(pricePolicyId)
                        .price(10000L)
                        .discountPrice(8000L)
                        .discountRate(new BigDecimal("20.0"))
                        .accumulatedPoint(200L)
                        .accumulationRate(new BigDecimal("2.5"))
                        .status(EntityStatus.ACTIVE)
                        .build()
        );

        return CartProduct.from(
                CartProductSnapshotState.builder()
                        .userId(userId)
                        .pricePolicy(pricePolicy)
                        .imageUrl("https://example.com/image.png")
                        .quantity(quantity)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
