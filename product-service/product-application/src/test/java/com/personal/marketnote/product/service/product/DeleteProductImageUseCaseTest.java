package com.personal.marketnote.product.service.product;

import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.port.in.command.DeleteProductImageCommand;
import com.personal.marketnote.product.port.out.file.DeleteProductImagesPort;
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
class DeleteProductImageUseCaseTest {
    @Mock
    private FindProductPort findProductPort;
    @Mock
    private DeleteProductImagesPort deleteProductImagesPort;

    @InjectMocks
    private DeleteProductImageService deleteProductImageService;

    @Test
    @DisplayName("상품 이미지 삭제 요청 회원이 관리자 또는 상품 판매자가 아니면 예외를 던진다")
    void delete_notOwner_throws() {
        Long userId = 1L;
        DeleteProductImageCommand command = DeleteProductImageCommand.of(10L, 100L);

        when(findProductPort.existsByIdAndSellerId(10L, userId)).thenReturn(false);

        assertThatThrownBy(() -> deleteProductImageService.delete(userId, false, command))
                .isInstanceOf(NotProductOwnerException.class)
                .hasMessageContaining("관리자 또는 상품 판매자가 아닙니다");

        verify(findProductPort).existsByIdAndSellerId(10L, userId);
        verifyNoInteractions(deleteProductImagesPort);
    }

    @Test
    @DisplayName("상품 이미지 삭제 시 관리자 요청인 경우 소유자 확인 없이 삭제한다")
    void delete_adminSkipsOwnerCheck_deletesImage() {
        DeleteProductImageCommand command = DeleteProductImageCommand.of(11L, 101L);

        deleteProductImageService.delete(99L, true, command);

        verify(deleteProductImagesPort).delete(101L);
        verifyNoInteractions(findProductPort);
    }

    @Test
    @DisplayName("상품 이미지 삭제 시 상품 판매자 요청인 경우 소유자 확인 후 삭제한다")
    void delete_owner_deletesImage() {
        Long userId = 2L;
        DeleteProductImageCommand command = DeleteProductImageCommand.of(20L, 201L);

        when(findProductPort.existsByIdAndSellerId(20L, userId)).thenReturn(true);

        deleteProductImageService.delete(userId, false, command);

        verify(findProductPort).existsByIdAndSellerId(20L, userId);
        verify(deleteProductImagesPort).delete(201L);
    }

    @Test
    @DisplayName("상품 이미지 삭제 시 삭제 요청에 실패하면 예외를 전파한다")
    void delete_deleteFails_propagates() {
        Long userId = 3L;
        DeleteProductImageCommand command = DeleteProductImageCommand.of(30L, 301L);
        RuntimeException exception = new RuntimeException("delete fail");

        when(findProductPort.existsByIdAndSellerId(30L, userId)).thenReturn(true);
        doThrow(exception).when(deleteProductImagesPort).delete(301L);

        assertThatThrownBy(() -> deleteProductImageService.delete(userId, false, command))
                .isSameAs(exception);

        verify(findProductPort).existsByIdAndSellerId(30L, userId);
        verify(deleteProductImagesPort).delete(301L);
    }
}
