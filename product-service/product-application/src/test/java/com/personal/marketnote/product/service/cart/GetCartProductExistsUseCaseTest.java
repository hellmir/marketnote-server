package com.personal.marketnote.product.service.cart;

import com.personal.marketnote.product.port.in.usecase.product.GetProductInventoryUseCase;
import com.personal.marketnote.product.port.out.cart.FindCartProductPort;
import com.personal.marketnote.product.port.out.cart.FindCartProductsPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCartProductExistsUseCaseTest {
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
}
