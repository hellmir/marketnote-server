package com.personal.marketnote.product.service.pricepolicy;

import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.port.out.pricepolicy.DeletePricePolicyPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeletePricePolicyUseCaseTest {
    @Mock
    private FindProductPort findProductPort;
    @Mock
    private DeletePricePolicyPort deletePricePolicyPort;

    @InjectMocks
    private DeletePricePolicyService deletePricePolicyService;

    @Test
    @DisplayName("가격 정책 삭제 요청 회원이 관리자 또는 상품 판매자가 아니면 예외를 던진다")
    void delete_notOwner_throws() {
        Long userId = 1L;
        Long productId = 10L;
        Long pricePolicyId = 100L;

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(false);

        assertThatThrownBy(() -> deletePricePolicyService.delete(userId, false, productId, pricePolicyId))
                .isInstanceOf(NotProductOwnerException.class)
                .hasMessageContaining("관리자 또는 상품 판매자가 아닙니다");

        verify(findProductPort).existsByIdAndSellerId(productId, userId);
        verifyNoInteractions(deletePricePolicyPort);
    }

    @Test
    @DisplayName("가격 정책 삭제 시 관리자 요청인 경우 소유자 확인 없이 삭제한다")
    void delete_admin_skipsOwnerCheck() {
        Long userId = 2L;
        Long productId = 20L;
        Long pricePolicyId = 200L;

        deletePricePolicyService.delete(userId, true, productId, pricePolicyId);

        verify(deletePricePolicyPort).deleteById(pricePolicyId);
        verifyNoInteractions(findProductPort);
    }

    @Test
    @DisplayName("가격 정책 삭제 시 상품 판매자 요청인 경우 소유자 확인 후 삭제한다")
    void delete_owner_deletes() {
        Long userId = 3L;
        Long productId = 30L;
        Long pricePolicyId = 300L;

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);

        deletePricePolicyService.delete(userId, false, productId, pricePolicyId);

        verify(findProductPort).existsByIdAndSellerId(productId, userId);
        verify(deletePricePolicyPort).deleteById(pricePolicyId);
    }

    @Test
    @DisplayName("가격 정책 삭제 시 삭제 요청에 실패하면 예외를 전파한다")
    void delete_deleteFails_propagates() {
        Long userId = 4L;
        Long productId = 40L;
        Long pricePolicyId = 400L;
        RuntimeException exception = new RuntimeException("delete fail");

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        doThrow(exception).when(deletePricePolicyPort).deleteById(pricePolicyId);

        assertThatThrownBy(() -> deletePricePolicyService.delete(userId, false, productId, pricePolicyId))
                .isSameAs(exception);

        verify(findProductPort).existsByIdAndSellerId(productId, userId);
        verify(deletePricePolicyPort).deleteById(pricePolicyId);
    }
}
