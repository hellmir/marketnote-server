package com.personal.marketnote.product.service.option;

import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productoption.DeleteProductOptionCategoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProductOptionsUseCaseTest {
    @Mock
    private FindProductPort findProductPort;
    @Mock
    private DeleteProductOptionCategoryPort deleteProductOptionCategoryPort;

    @InjectMocks
    private DeleteProductOptionsService deleteProductOptionsService;

    @Test
    @DisplayName("상품 옵션 삭제 요청 회원이 관리자 또는 상품 판매자가 아니면 예외를 던진다")
    void delete_notOwner_throws() {
        Long userId = 1L;
        Long productId = 10L;
        Long optionCategoryId = 100L;

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(false);

        assertThatThrownBy(() -> deleteProductOptionsService.deleteProductOptions(
                userId, false, productId, optionCategoryId
        ))
                .isInstanceOf(NotProductOwnerException.class)
                .hasMessageContaining("관리자 또는 상품 판매자가 아닙니다");

        verify(findProductPort).existsByIdAndSellerId(productId, userId);
        verifyNoInteractions(deleteProductOptionCategoryPort);
    }

    @Test
    @DisplayName("상품 옵션 삭제 시 관리자 요청인 경우 소유자 확인 없이 삭제한다")
    void delete_admin_skipsOwnerCheck() {
        Long userId = 2L;
        Long productId = 20L;
        Long optionCategoryId = 200L;

        deleteProductOptionsService.deleteProductOptions(userId, true, productId, optionCategoryId);

        verify(deleteProductOptionCategoryPort).deleteById(optionCategoryId);
        verifyNoInteractions(findProductPort);
    }

    @Test
    @DisplayName("상품 옵션 삭제 시 상품 판매자 요청인 경우 소유자 확인 후 삭제한다")
    void delete_owner_deletes() {
        Long userId = 3L;
        Long productId = 30L;
        Long optionCategoryId = 300L;

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);

        deleteProductOptionsService.deleteProductOptions(userId, false, productId, optionCategoryId);

        verify(findProductPort).existsByIdAndSellerId(productId, userId);
        verify(deleteProductOptionCategoryPort).deleteById(optionCategoryId);
    }

    @Test
    @DisplayName("상품 옵션 삭제 시 삭제 요청에 실패하면 예외를 전파한다")
    void delete_deleteFails_propagates() {
        Long userId = 4L;
        Long productId = 40L;
        Long optionCategoryId = 400L;
        RuntimeException exception = new RuntimeException("delete fail");

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        doThrow(exception).when(deleteProductOptionCategoryPort).deleteById(optionCategoryId);

        assertThatThrownBy(() -> deleteProductOptionsService.deleteProductOptions(
                userId, false, productId, optionCategoryId
        )).isSameAs(exception);

        verify(findProductPort).existsByIdAndSellerId(productId, userId);
        verify(deleteProductOptionCategoryPort).deleteById(optionCategoryId);
    }
}
