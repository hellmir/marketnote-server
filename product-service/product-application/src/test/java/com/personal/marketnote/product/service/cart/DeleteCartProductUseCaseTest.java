package com.personal.marketnote.product.service.cart;

import com.personal.marketnote.product.port.in.command.DeleteCartProductCommand;
import com.personal.marketnote.product.port.out.cart.DeleteCartProductPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteCartProductUseCaseTest {
    @Mock
    private DeleteCartProductPort deleteCartProductPort;

    @InjectMocks
    private DeleteCartProductService deleteCartProductService;

    @Test
    @DisplayName("장바구니 상품 삭제 시 가격 정책 ID 목록으로 삭제를 요청한다")
    void deleteCartProducts_deletesByPricePolicyIds() {
        DeleteCartProductCommand command = DeleteCartProductCommand.of(1L, List.of(10L, 20L));

        deleteCartProductService.deleteCartProducts(command);

        verify(deleteCartProductPort).delete(1L, List.of(10L, 20L));
    }

    @Test
    @DisplayName("장바구니 상품 삭제 시 삭제 요청에 실패하면 예외를 전파한다")
    void deleteCartProducts_deleteFails_propagates() {
        DeleteCartProductCommand command = DeleteCartProductCommand.of(2L, List.of(30L));
        RuntimeException exception = new RuntimeException("delete fail");

        doThrow(exception).when(deleteCartProductPort).delete(2L, List.of(30L));

        assertThatThrownBy(() -> deleteCartProductService.deleteCartProducts(command))
                .isSameAs(exception);
    }
}
