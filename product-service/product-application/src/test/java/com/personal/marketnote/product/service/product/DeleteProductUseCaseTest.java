package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.exception.ProductNotFoundException;
import com.personal.marketnote.product.port.in.command.DeleteProductCommand;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.product.UpdateProductPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProductUseCaseTest {
    @Mock
    private GetProductUseCase getProductUseCase;
    @Mock
    private FindProductPort findProductPort;
    @Mock
    private UpdateProductPort updateProductPort;

    @InjectMocks
    private DeleteProductService deleteProductService;

    @Test
    @DisplayName("상품 삭제 요청 회원이 관리자 또는 상품 판매자가 아니면 예외를 던진다")
    void delete_notOwner_throws() {
        Long userId = 1L;
        DeleteProductCommand command = DeleteProductCommand.of(10L);

        when(findProductPort.existsByIdAndSellerId(10L, userId)).thenReturn(false);

        assertThatThrownBy(() -> deleteProductService.delete(userId, false, command))
                .isInstanceOf(NotProductOwnerException.class)
                .hasMessageContaining("관리자 또는 상품 판매자가 아닙니다");

        verify(findProductPort).existsByIdAndSellerId(10L, userId);
        verifyNoInteractions(getProductUseCase, updateProductPort);
    }

    @Test
    @DisplayName("상품 삭제 시 관리자 요청인 경우 소유자 여부 확인 없이 상품을 비활성화한다")
    void delete_adminSkipsOwnerCheck_deactivatesProduct() {
        DeleteProductCommand command = DeleteProductCommand.of(11L);
        Product product = buildProduct(11L);

        when(getProductUseCase.getProduct(11L)).thenReturn(product);

        deleteProductService.delete(99L, true, command);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(updateProductPort).update(productCaptor.capture());
        Product updated = productCaptor.getValue();

        assertThat(updated.isInactive()).isTrue();
        assertThat(updated.getStatus()).isEqualTo(EntityStatus.INACTIVE);

        verify(getProductUseCase).getProduct(11L);
        verifyNoInteractions(findProductPort);
    }

    @Test
    @DisplayName("상품 삭제 시 상품 판매자 요청인 경우, 본인 판매 상품 확인 후 상품을 비활성화한다")
    void delete_owner_deactivatesProduct() {
        Long userId = 2L;
        DeleteProductCommand command = DeleteProductCommand.of(20L);
        Product product = buildProduct(20L);

        when(findProductPort.existsByIdAndSellerId(20L, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(20L)).thenReturn(product);

        deleteProductService.delete(userId, false, command);

        verify(findProductPort).existsByIdAndSellerId(20L, userId);
        verify(updateProductPort).update(product);
        assertThat(product.isInactive()).isTrue();
    }

    @Test
    @DisplayName("상품 삭제 시 상품 조회가 실패하면 예외를 전파한다")
    void delete_getProductFails_propagates() {
        DeleteProductCommand command = DeleteProductCommand.of(30L);
        ProductNotFoundException exception = new ProductNotFoundException(30L);

        when(getProductUseCase.getProduct(30L)).thenThrow(exception);

        assertThatThrownBy(() -> deleteProductService.delete(1L, true, command))
                .isSameAs(exception);

        verify(getProductUseCase).getProduct(30L);
        verifyNoInteractions(findProductPort, updateProductPort);
    }

    @Test
    @DisplayName("상품 삭제 시 데이터 업데이트에 실패하면 예외를 전파한다")
    void delete_updateFails_propagates() {
        Long userId = 3L;
        DeleteProductCommand command = DeleteProductCommand.of(40L);
        Product product = buildProduct(40L);
        ProductNotFoundException exception = new ProductNotFoundException(40L);

        when(findProductPort.existsByIdAndSellerId(40L, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(40L)).thenReturn(product);
        doThrow(exception).when(updateProductPort).update(product);

        assertThatThrownBy(() -> deleteProductService.delete(userId, false, command))
                .isSameAs(exception);

        verify(updateProductPort).update(product);
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
                        .productTags(java.util.List.of())
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
