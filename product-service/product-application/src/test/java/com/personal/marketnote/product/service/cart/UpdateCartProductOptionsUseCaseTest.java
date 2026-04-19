package com.personal.marketnote.product.service.cart;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.domain.cart.CartProductSnapshotState;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.exception.CartProductAlreadyExistsException;
import com.personal.marketnote.product.exception.CartProductNotFoundException;
import com.personal.marketnote.product.port.in.command.UpdateCartProductOptionCommand;
import com.personal.marketnote.product.port.in.usecase.cart.GetCartProductUseCase;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePolicyUseCase;
import com.personal.marketnote.product.port.out.cart.UpdateCartProductPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCartProductOptionsUseCaseTest {
    @Mock
    private GetCartProductUseCase getCartProductUseCase;
    @Mock
    private GetPricePolicyUseCase getPricePolicyUseCase;
    @Mock
    private UpdateCartProductPort updateCartProductPort;

    @InjectMocks
    private UpdateCartProductOptionsService updateCartProductOptionsService;

    @Test
    @DisplayName("장바구니 상품 옵션 변경 시 새 가격 정책으로 변경하고 저장한다")
    void updateCartProductOptions_success_updatesAndSaves() {
        // given
        Long userId = 1L;
        Long originalPolicyId = 100L;
        Long newPolicyId = 200L;
        List<Long> newOptionIds = List.of(10L, 20L);

        UpdateCartProductOptionCommand command =
                UpdateCartProductOptionCommand.of(userId, originalPolicyId, newOptionIds);
        CartProduct cartProduct = buildCartProduct(userId, originalPolicyId, (short) 2);
        PricePolicy newPricePolicy = buildPricePolicy(newPolicyId);

        when(getCartProductUseCase.getCartProduct(userId, originalPolicyId)).thenReturn(cartProduct);
        when(getPricePolicyUseCase.getPricePolicy(newOptionIds)).thenReturn(newPricePolicy);
        when(getCartProductUseCase.existsByUserIdAndPolicyId(userId, newPolicyId)).thenReturn(false);

        // when
        updateCartProductOptionsService.updateCartProductOptions(command);

        // then
        assertThat(cartProduct.getPricePolicyId()).isEqualTo(newPolicyId);

        InOrder inOrder = inOrder(getCartProductUseCase, getPricePolicyUseCase, updateCartProductPort);
        inOrder.verify(getCartProductUseCase).getCartProduct(userId, originalPolicyId);
        inOrder.verify(getPricePolicyUseCase).getPricePolicy(newOptionIds);
        inOrder.verify(updateCartProductPort).update(cartProduct, originalPolicyId);
    }

    @Test
    @DisplayName("옵션 변경 결과 동일한 가격 정책이면 중복 검사 없이 저장한다")
    void updateCartProductOptions_samePolicyId_skipsExistsCheckAndSaves() {
        // given
        Long userId = 1L;
        Long policyId = 100L;
        List<Long> newOptionIds = List.of(10L, 20L);

        UpdateCartProductOptionCommand command =
                UpdateCartProductOptionCommand.of(userId, policyId, newOptionIds);
        CartProduct cartProduct = buildCartProduct(userId, policyId, (short) 2);
        PricePolicy samePricePolicy = buildPricePolicy(policyId);

        when(getCartProductUseCase.getCartProduct(userId, policyId)).thenReturn(cartProduct);
        when(getPricePolicyUseCase.getPricePolicy(newOptionIds)).thenReturn(samePricePolicy);

        // when
        updateCartProductOptionsService.updateCartProductOptions(command);

        // then
        verify(getCartProductUseCase, never()).existsByUserIdAndPolicyId(anyLong(), anyLong());
        verify(updateCartProductPort).update(cartProduct, policyId);
    }

    @Test
    @DisplayName("변경된 가격 정책이 이미 장바구니에 존재하면 CartProductAlreadyExistsException을 던진다")
    void updateCartProductOptions_duplicatePolicy_throwsException() {
        // given
        Long userId = 1L;
        Long originalPolicyId = 100L;
        Long newPolicyId = 200L;
        List<Long> newOptionIds = List.of(30L, 40L);

        UpdateCartProductOptionCommand command =
                UpdateCartProductOptionCommand.of(userId, originalPolicyId, newOptionIds);
        CartProduct cartProduct = buildCartProduct(userId, originalPolicyId, (short) 1);
        PricePolicy newPricePolicy = buildPricePolicy(newPolicyId);

        when(getCartProductUseCase.getCartProduct(userId, originalPolicyId)).thenReturn(cartProduct);
        when(getPricePolicyUseCase.getPricePolicy(newOptionIds)).thenReturn(newPricePolicy);
        when(getCartProductUseCase.existsByUserIdAndPolicyId(userId, newPolicyId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> updateCartProductOptionsService.updateCartProductOptions(command))
                .isInstanceOf(CartProductAlreadyExistsException.class);

        verifyNoInteractions(updateCartProductPort);
    }

    @Test
    @DisplayName("장바구니 상품 조회에 실패하면 예외를 전파한다")
    void updateCartProductOptions_cartProductNotFound_propagatesException() {
        // given
        Long userId = 1L;
        Long pricePolicyId = 999L;
        List<Long> newOptionIds = List.of(10L);

        UpdateCartProductOptionCommand command =
                UpdateCartProductOptionCommand.of(userId, pricePolicyId, newOptionIds);
        CartProductNotFoundException exception = new CartProductNotFoundException(pricePolicyId);

        when(getCartProductUseCase.getCartProduct(userId, pricePolicyId)).thenThrow(exception);

        // when & then
        assertThatThrownBy(() -> updateCartProductOptionsService.updateCartProductOptions(command))
                .isSameAs(exception);

        verifyNoInteractions(getPricePolicyUseCase);
        verifyNoInteractions(updateCartProductPort);
    }

    @Test
    @DisplayName("새 옵션에 해당하는 가격 정책 조회에 실패하면 예외를 전파한다")
    void updateCartProductOptions_pricePolicyNotFound_propagatesException() {
        // given
        Long userId = 1L;
        Long originalPolicyId = 100L;
        List<Long> newOptionIds = List.of(99L);

        UpdateCartProductOptionCommand command =
                UpdateCartProductOptionCommand.of(userId, originalPolicyId, newOptionIds);
        CartProduct cartProduct = buildCartProduct(userId, originalPolicyId, (short) 1);
        RuntimeException exception = new RuntimeException("price policy not found");

        when(getCartProductUseCase.getCartProduct(userId, originalPolicyId)).thenReturn(cartProduct);
        when(getPricePolicyUseCase.getPricePolicy(newOptionIds)).thenThrow(exception);

        // when & then
        assertThatThrownBy(() -> updateCartProductOptionsService.updateCartProductOptions(command))
                .isSameAs(exception);

        verifyNoInteractions(updateCartProductPort);
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

    private CartProduct buildCartProduct(Long userId, Long pricePolicyId, Short quantity) {
        PricePolicy pricePolicy = buildPricePolicy(pricePolicyId);

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
