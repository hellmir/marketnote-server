package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.domain.product.ProductTagSnapshotState;
import com.personal.marketnote.product.exception.DuplicateProductTagOrderException;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.exception.ProductNotFoundException;
import com.personal.marketnote.product.exception.ProductTagNotFoundException;
import com.personal.marketnote.product.port.in.command.ReorderProductTagsCommand;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.product.UpdateProductTagPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReorderProductTagsUseCaseTest {
    @Mock
    private GetProductUseCase getProductUseCase;
    @Mock
    private FindProductPort findProductPort;
    @Mock
    private UpdateProductTagPort updateProductTagPort;

    @InjectMocks
    private ReorderProductTagsService reorderProductTagsService;

    @Test
    @DisplayName("관리자가 상품 태그 순서를 변경하면 소유권 검증 없이 성공한다")
    void reorderProductTags_admin_skipsOwnerCheck() {
        Long productId = 10L;
        Product product = buildProductWithTags(productId, 1L);
        ReorderProductTagsCommand command = buildCommand(productId, List.of(
                new ReorderProductTagsCommand.TagOrderItem(100L, 2L),
                new ReorderProductTagsCommand.TagOrderItem(200L, 1L)
        ));

        when(getProductUseCase.getProduct(productId)).thenReturn(product);

        reorderProductTagsService.reorderProductTags(99L, true, command);

        verify(getProductUseCase).getProduct(productId);
        verifyNoInteractions(findProductPort);

        ArgumentCaptor<Map<Long, Long>> mapCaptor = captureTagIdToOrderNumMap(productId);
        Map<Long, Long> captured = mapCaptor.getValue();
        assertThat(captured).containsEntry(100L, 2L);
        assertThat(captured).containsEntry(200L, 1L);
    }

    @Test
    @DisplayName("판매자(소유자)가 상품 태그 순서를 변경하면 성공한다")
    void reorderProductTags_ownerSeller_succeeds() {
        Long productId = 20L;
        Long sellerId = 5L;
        Product product = buildProductWithTags(productId, sellerId);
        ReorderProductTagsCommand command = buildCommand(productId, List.of(
                new ReorderProductTagsCommand.TagOrderItem(100L, 3L),
                new ReorderProductTagsCommand.TagOrderItem(200L, 1L)
        ));

        when(findProductPort.existsByIdAndSellerId(productId, sellerId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);

        reorderProductTagsService.reorderProductTags(sellerId, false, command);

        verify(findProductPort).existsByIdAndSellerId(productId, sellerId);
        verify(getProductUseCase).getProduct(productId);

        ArgumentCaptor<Map<Long, Long>> mapCaptor = captureTagIdToOrderNumMap(productId);
        Map<Long, Long> captured = mapCaptor.getValue();
        assertThat(captured).containsEntry(100L, 3L);
        assertThat(captured).containsEntry(200L, 1L);
    }

    @Test
    @DisplayName("판매자(비소유자)가 상품 태그 순서를 변경하면 NotProductOwnerException이 발생한다")
    void reorderProductTags_nonOwnerSeller_throws() {
        Long productId = 30L;
        Long nonOwnerId = 99L;
        ReorderProductTagsCommand command = buildCommand(productId, List.of(
                new ReorderProductTagsCommand.TagOrderItem(100L, 1L)
        ));

        when(findProductPort.existsByIdAndSellerId(productId, nonOwnerId)).thenReturn(false);

        assertThatThrownBy(() -> reorderProductTagsService.reorderProductTags(nonOwnerId, false, command))
                .isInstanceOf(NotProductOwnerException.class)
                .hasMessageContaining("관리자 또는 상품 판매자가 아닙니다");

        verify(findProductPort).existsByIdAndSellerId(productId, nonOwnerId);
        verifyNoInteractions(getProductUseCase, updateProductTagPort);
    }

    @Test
    @DisplayName("존재하지 않는 태그 ID가 포함되면 ProductTagNotFoundException이 발생한다")
    void reorderProductTags_nonExistentTagId_throws() {
        Long productId = 40L;
        Long nonExistentTagId = 999L;
        Product product = buildProductWithTags(productId, 1L);
        ReorderProductTagsCommand command = buildCommand(productId, List.of(
                new ReorderProductTagsCommand.TagOrderItem(100L, 1L),
                new ReorderProductTagsCommand.TagOrderItem(nonExistentTagId, 2L)
        ));

        when(getProductUseCase.getProduct(productId)).thenReturn(product);

        assertThatThrownBy(() -> reorderProductTagsService.reorderProductTags(1L, true, command))
                .isInstanceOf(ProductTagNotFoundException.class)
                .hasMessageContaining("tagId=" + nonExistentTagId)
                .hasMessageContaining("productId=" + productId);

        verify(getProductUseCase).getProduct(productId);
        verifyNoInteractions(updateProductTagPort);
    }

    @Test
    @DisplayName("상품 조회에 실패하면 예외를 전파한다")
    void reorderProductTags_productNotFound_propagates() {
        Long productId = 50L;
        ReorderProductTagsCommand command = buildCommand(productId, List.of(
                new ReorderProductTagsCommand.TagOrderItem(100L, 1L)
        ));
        ProductNotFoundException exception = new ProductNotFoundException(productId);

        when(getProductUseCase.getProduct(productId)).thenThrow(exception);

        assertThatThrownBy(() -> reorderProductTagsService.reorderProductTags(1L, true, command))
                .isSameAs(exception);

        verify(getProductUseCase).getProduct(productId);
        verifyNoInteractions(findProductPort, updateProductTagPort);
    }

    @Test
    @DisplayName("중복된 태그 ID가 포함되면 DuplicateProductTagOrderException이 발생한다")
    void reorderProductTags_duplicateTagId_throws() {
        Long productId = 60L;
        Product product = buildProductWithTags(productId, 1L);
        ReorderProductTagsCommand command = buildCommand(productId, List.of(
                new ReorderProductTagsCommand.TagOrderItem(100L, 1L),
                new ReorderProductTagsCommand.TagOrderItem(100L, 2L)
        ));

        when(getProductUseCase.getProduct(productId)).thenReturn(product);

        assertThatThrownBy(() -> reorderProductTagsService.reorderProductTags(1L, true, command))
                .isInstanceOf(DuplicateProductTagOrderException.class)
                .hasMessageContaining("중복된 태그 ID")
                .hasMessageContaining("tagId=100");

        verifyNoInteractions(updateProductTagPort);
    }

    @Test
    @DisplayName("중복된 순서 번호가 포함되면 DuplicateProductTagOrderException이 발생한다")
    void reorderProductTags_duplicateOrderNum_throws() {
        Long productId = 70L;
        Product product = buildProductWithTags(productId, 1L);
        ReorderProductTagsCommand command = buildCommand(productId, List.of(
                new ReorderProductTagsCommand.TagOrderItem(100L, 1L),
                new ReorderProductTagsCommand.TagOrderItem(200L, 1L)
        ));

        when(getProductUseCase.getProduct(productId)).thenReturn(product);

        assertThatThrownBy(() -> reorderProductTagsService.reorderProductTags(1L, true, command))
                .isInstanceOf(DuplicateProductTagOrderException.class)
                .hasMessageContaining("중복된 순서 번호")
                .hasMessageContaining("orderNum=1");

        verifyNoInteractions(updateProductTagPort);
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<Long, Long>> captureTagIdToOrderNumMap(Long productId) {
        ArgumentCaptor<Map<Long, Long>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(updateProductTagPort).updateOrderNums(eq(productId), mapCaptor.capture());
        return mapCaptor;
    }

    private ReorderProductTagsCommand buildCommand(Long productId, List<ReorderProductTagsCommand.TagOrderItem> tagOrders) {
        return ReorderProductTagsCommand.builder()
                .productId(productId)
                .tagOrders(tagOrders)
                .build();
    }

    private Product buildProductWithTags(Long productId, Long sellerId) {
        List<ProductTag> tags = List.of(
                ProductTag.from(ProductTagSnapshotState.builder()
                        .id(100L).productId(productId).name("태그1").orderNum(1L).status(EntityStatus.ACTIVE).build()),
                ProductTag.from(ProductTagSnapshotState.builder()
                        .id(200L).productId(productId).name("태그2").orderNum(2L).status(EntityStatus.ACTIVE).build())
        );

        return Product.from(
                ProductSnapshotState.builder()
                        .id(productId)
                        .sellerId(sellerId)
                        .name("테스트 상품")
                        .brandName("테스트 브랜드")
                        .detail("테스트 상세")
                        .findAllOptionsYn(false)
                        .productTags(tags)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
