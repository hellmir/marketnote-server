package com.personal.marketnote.product.service.option;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;
import com.personal.marketnote.product.domain.option.ProductOptionCategorySnapshotState;
import com.personal.marketnote.product.domain.option.ProductOptionSnapshotState;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.exception.OptionsNoValueException;
import com.personal.marketnote.product.port.in.command.RegisterProductOptionsCommand;
import com.personal.marketnote.product.port.in.command.UpdateProductOptionsCommand;
import com.personal.marketnote.product.port.in.result.option.UpdateProductOptionsResult;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productoption.DeleteProductOptionCategoryPort;
import com.personal.marketnote.product.port.out.productoption.SaveProductOptionsPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProductOptionsUseCaseTest {
    @Mock
    private GetProductUseCase getProductUseCase;
    @Mock
    private FindProductPort findProductPort;
    @Mock
    private SaveProductOptionsPort saveProductOptionsPort;
    @Mock
    private DeleteProductOptionCategoryPort deleteProductOptionCategoryPort;

    @InjectMocks
    private UpdateProductOptionsService updateProductOptionsService;

    @Test
    @DisplayName("상품 옵션 수정 요청 회원이 관리자 또는 상품 판매자가 아니면 예외를 던진다")
    void update_notOwner_throws() {
        Long userId = 1L;
        UpdateProductOptionsCommand command = buildCommand(10L, 100L, "옵션", List.of("옵션1"));

        when(findProductPort.existsByIdAndSellerId(10L, userId)).thenReturn(false);

        assertThatThrownBy(() -> updateProductOptionsService.updateProductOptions(userId, false, command))
                .isInstanceOf(NotProductOwnerException.class)
                .hasMessageContaining("관리자 또는 상품 판매자가 아닙니다");

        verify(findProductPort).existsByIdAndSellerId(10L, userId);
        verifyNoInteractions(getProductUseCase, deleteProductOptionCategoryPort, saveProductOptionsPort);
    }

    @Test
    @DisplayName("상품 옵션 수정 시 옵션 목록이 null이면 예외를 던진다")
    void update_optionsNull_throws() {
        Long userId = 2L;
        Long productId = 20L;
        UpdateProductOptionsCommand command = UpdateProductOptionsCommand.of(productId, 200L, "옵션", null);
        Product product = buildProduct(productId, userId);

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);

        assertThatThrownBy(() -> updateProductOptionsService.updateProductOptions(userId, false, command))
                .isInstanceOf(OptionsNoValueException.class)
                .hasMessageContaining("옵션을 포함해야 합니다");

        verify(findProductPort).existsByIdAndSellerId(productId, userId);
        verify(getProductUseCase).getProduct(productId);
        verifyNoInteractions(deleteProductOptionCategoryPort, saveProductOptionsPort);
    }

    @Test
    @DisplayName("상품 옵션 수정 시 옵션 목록이 비어 있으면 예외를 던진다")
    void update_optionsEmpty_throws() {
        Long userId = 3L;
        Long productId = 30L;
        UpdateProductOptionsCommand command = buildCommand(productId, 300L, "옵션", List.of());
        Product product = buildProduct(productId, userId);

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);

        assertThatThrownBy(() -> updateProductOptionsService.updateProductOptions(userId, false, command))
                .isInstanceOf(OptionsNoValueException.class)
                .hasMessageContaining("옵션을 포함해야 합니다");

        verify(findProductPort).existsByIdAndSellerId(productId, userId);
        verify(getProductUseCase).getProduct(productId);
        verifyNoInteractions(deleteProductOptionCategoryPort, saveProductOptionsPort);
    }

    @Test
    @DisplayName("상품 옵션 수정 시 관리자 요청이면 소유자 확인 없이 수정한다")
    void update_admin_skipsOwnerCheck() {
        Long userId = 4L;
        Long productId = 40L;
        Long categoryId = 400L;
        UpdateProductOptionsCommand command = buildCommand(productId, categoryId, "옵션", List.of("옵션1"));
        Product product = buildProduct(productId, 999L);
        ProductOptionCategory savedCategory = buildSavedCategory(100L, product, List.of(1001L));

        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(saveProductOptionsPort.save(any(ProductOptionCategory.class))).thenReturn(savedCategory);

        UpdateProductOptionsResult result = updateProductOptionsService.updateProductOptions(
                userId, true, command
        );

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.optionIds()).containsExactly(1001L);
        verifyNoInteractions(findProductPort);
        verify(deleteProductOptionCategoryPort).deleteById(categoryId);
    }

    @Test
    @DisplayName("상품 옵션 수정 시 기존 카테고리를 삭제하고 새 옵션을 저장한다")
    void update_success_deletesAndSaves() {
        Long userId = 5L;
        Long productId = 50L;
        Long categoryId = 500L;
        UpdateProductOptionsCommand command = buildCommand(productId, categoryId, "색상", List.of("RED", "BLUE"));
        Product product = buildProduct(productId, userId);
        ProductOptionCategory savedCategory = buildSavedCategory(200L, product, List.of(2001L, 2002L));

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(saveProductOptionsPort.save(any(ProductOptionCategory.class))).thenReturn(savedCategory);

        UpdateProductOptionsResult result = updateProductOptionsService.updateProductOptions(
                userId, false, command
        );

        assertThat(result.id()).isEqualTo(200L);
        assertThat(result.optionIds()).containsExactly(2001L, 2002L);

        InOrder inOrder = inOrder(deleteProductOptionCategoryPort, saveProductOptionsPort);
        inOrder.verify(deleteProductOptionCategoryPort).deleteById(categoryId);
        inOrder.verify(saveProductOptionsPort).save(any(ProductOptionCategory.class));

        ArgumentCaptor<ProductOptionCategory> captor = ArgumentCaptor.forClass(ProductOptionCategory.class);
        verify(saveProductOptionsPort).save(captor.capture());
        ProductOptionCategory requestCategory = captor.getValue();

        assertThat(requestCategory.getProduct()).isSameAs(product);
        assertThat(requestCategory.getName()).isEqualTo("색상");
        assertThat(requestCategory.getOptions())
                .extracting(ProductOption::getContent)
                .containsExactly("RED", "BLUE");
        assertThat(requestCategory.getOptions())
                .extracting(ProductOption::getStatus)
                .containsOnly(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("상품 옵션 수정 시 상품 조회에 실패하면 예외를 전파한다")
    void update_getProductFails_propagates() {
        Long userId = 6L;
        Long productId = 60L;
        UpdateProductOptionsCommand command = buildCommand(productId, 600L, "옵션", List.of("옵션1"));
        RuntimeException exception = new RuntimeException("product fail");

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenThrow(exception);

        assertThatThrownBy(() -> updateProductOptionsService.updateProductOptions(userId, false, command))
                .isSameAs(exception);

        verifyNoInteractions(deleteProductOptionCategoryPort, saveProductOptionsPort);
    }

    @Test
    @DisplayName("상품 옵션 수정 시 기존 카테고리 삭제에 실패하면 예외를 전파한다")
    void update_deleteFails_propagates() {
        Long userId = 7L;
        Long productId = 70L;
        Long categoryId = 700L;
        UpdateProductOptionsCommand command = buildCommand(productId, categoryId, "옵션", List.of("옵션1"));
        Product product = buildProduct(productId, userId);
        RuntimeException exception = new RuntimeException("delete fail");

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        doThrow(exception).when(deleteProductOptionCategoryPort).deleteById(categoryId);

        assertThatThrownBy(() -> updateProductOptionsService.updateProductOptions(userId, false, command))
                .isSameAs(exception);

        verifyNoInteractions(saveProductOptionsPort);
    }

    @Test
    @DisplayName("상품 옵션 수정 시 저장에 실패하면 예외를 전파한다")
    void update_saveFails_propagates() {
        Long userId = 8L;
        Long productId = 80L;
        Long categoryId = 800L;
        UpdateProductOptionsCommand command = buildCommand(productId, categoryId, "옵션", List.of("옵션1"));
        Product product = buildProduct(productId, userId);
        RuntimeException exception = new RuntimeException("save fail");

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(saveProductOptionsPort.save(any(ProductOptionCategory.class))).thenThrow(exception);

        assertThatThrownBy(() -> updateProductOptionsService.updateProductOptions(userId, false, command))
                .isSameAs(exception);
    }

    private UpdateProductOptionsCommand buildCommand(
            Long productId,
            Long optionCategoryId,
            String categoryName,
            List<String> optionContents
    ) {
        List<RegisterProductOptionsCommand.OptionItem> options = optionContents
                .stream()
                .map(RegisterProductOptionsCommand.OptionItem::new)
                .toList();
        return UpdateProductOptionsCommand.of(productId, optionCategoryId, categoryName, options);
    }

    private Product buildProduct(Long productId, Long sellerId) {
        return Product.from(
                ProductSnapshotState.builder()
                        .id(productId)
                        .sellerId(sellerId)
                        .name("테스트 상품")
                        .brandName("브랜드")
                        .detail("상세 설명")
                        .findAllOptionsYn(false)
                        .productTags(List.of())
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private ProductOptionCategory buildSavedCategory(Long id, Product product, List<Long> optionIds) {
        List<ProductOption> options = optionIds.stream()
                .map(
                        optionId -> ProductOption.from(
                                ProductOptionSnapshotState.builder()
                                        .id(optionId)
                                        .content("opt-" + optionId)
                                        .status(EntityStatus.ACTIVE)
                                        .build()
                        )
                )
                .toList();

        return ProductOptionCategory.from(
                ProductOptionCategorySnapshotState.builder()
                        .id(id)
                        .product(product)
                        .name("옵션")
                        .options(options)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
