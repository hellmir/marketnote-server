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
import com.personal.marketnote.product.port.in.result.option.UpdateProductOptionsResult;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productoption.SaveProductOptionsPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterProductOptionsUseCaseTest {
    @Mock
    private GetProductUseCase getProductUseCase;
    @Mock
    private FindProductPort findProductPort;
    @Mock
    private SaveProductOptionsPort saveProductOptionsPort;

    @InjectMocks
    private RegisterProductOptionsService registerProductOptionsService;

    @Test
    @DisplayName("상품 옵션 등록 요청 회원이 관리자 또는 상품 판매자가 아니면 예외를 던진다")
    void register_notOwner_throws() {
        Long userId = 1L;
        RegisterProductOptionsCommand command = buildCommand(10L, List.of("옵션1"));

        when(findProductPort.existsByIdAndSellerId(10L, userId)).thenReturn(false);

        assertThatThrownBy(() -> registerProductOptionsService.registerProductOptions(userId, false, command))
                .isInstanceOf(NotProductOwnerException.class)
                .hasMessageContaining("관리자 또는 상품 판매자가 아닙니다");

        verify(findProductPort).existsByIdAndSellerId(10L, userId);
        verifyNoInteractions(getProductUseCase, saveProductOptionsPort);
    }

    @Test
    @DisplayName("상품 옵션 등록 시 옵션 목록이 null이면 예외를 던진다")
    void register_optionsNull_throws() {
        Long userId = 2L;
        Long productId = 20L;
        RegisterProductOptionsCommand command = RegisterProductOptionsCommand.of(productId, "옵션", null);
        Product product = buildProduct(productId, userId);

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);

        assertThatThrownBy(() -> registerProductOptionsService.registerProductOptions(userId, false, command))
                .isInstanceOf(OptionsNoValueException.class)
                .hasMessageContaining("옵션을 포함해야 합니다");

        verify(findProductPort).existsByIdAndSellerId(productId, userId);
        verify(getProductUseCase).getProduct(productId);
        verifyNoInteractions(saveProductOptionsPort);
    }

    @Test
    @DisplayName("상품 옵션 등록 시 옵션 목록이 비어 있으면 예외를 던진다")
    void register_optionsEmpty_throws() {
        Long userId = 3L;
        Long productId = 30L;
        RegisterProductOptionsCommand command = buildCommand(productId, List.of());
        Product product = buildProduct(productId, userId);

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);

        assertThatThrownBy(() -> registerProductOptionsService.registerProductOptions(userId, false, command))
                .isInstanceOf(OptionsNoValueException.class)
                .hasMessageContaining("옵션을 포함해야 합니다");

        verify(findProductPort).existsByIdAndSellerId(productId, userId);
        verify(getProductUseCase).getProduct(productId);
        verifyNoInteractions(saveProductOptionsPort);
    }

    @Test
    @DisplayName("상품 옵션 등록 시 관리자 요청이면 소유자 확인 없이 등록한다")
    void register_admin_skipsOwnerCheck() {
        Long userId = 4L;
        Long productId = 40L;
        RegisterProductOptionsCommand command = buildCommand(productId, List.of("옵션1"));
        Product product = buildProduct(productId, 999L);
        ProductOptionCategory savedCategory = buildSavedCategory(100L, product, List.of(1001L));

        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(saveProductOptionsPort.save(any(ProductOptionCategory.class))).thenReturn(savedCategory);

        UpdateProductOptionsResult result = registerProductOptionsService.registerProductOptions(
                userId, true, command
        );

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.optionIds()).containsExactly(1001L);
        verifyNoInteractions(findProductPort);
    }

    @Test
    @DisplayName("상품 옵션 등록 시 옵션을 저장하고 결과를 반환한다")
    void register_success_returnsResult() {
        Long userId = 5L;
        Long productId = 50L;
        RegisterProductOptionsCommand command = buildCommand(productId, List.of("옵션1", "옵션2"));
        Product product = buildProduct(productId, userId);
        ProductOptionCategory savedCategory = buildSavedCategory(200L, product, List.of(2001L, 2002L));

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(saveProductOptionsPort.save(any(ProductOptionCategory.class))).thenReturn(savedCategory);

        UpdateProductOptionsResult result = registerProductOptionsService.registerProductOptions(
                userId, false, command
        );

        assertThat(result.id()).isEqualTo(200L);
        assertThat(result.optionIds()).containsExactly(2001L, 2002L);

        ArgumentCaptor<ProductOptionCategory> captor = ArgumentCaptor.forClass(ProductOptionCategory.class);
        verify(saveProductOptionsPort).save(captor.capture());
        ProductOptionCategory requestCategory = captor.getValue();

        assertThat(requestCategory.getProduct()).isSameAs(product);
        assertThat(requestCategory.getName()).isEqualTo("옵션");
        assertThat(requestCategory.getOptions())
                .extracting(ProductOption::getContent)
                .containsExactly("옵션1", "옵션2");
        assertThat(requestCategory.getOptions())
                .extracting(ProductOption::getStatus)
                .containsOnly(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("상품 옵션 등록 시 상품 조회에 실패하면 예외를 전파한다")
    void register_getProductFails_propagates() {
        Long userId = 6L;
        Long productId = 60L;
        RegisterProductOptionsCommand command = buildCommand(productId, List.of("옵션1"));
        RuntimeException exception = new RuntimeException("product fail");

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenThrow(exception);

        assertThatThrownBy(() -> registerProductOptionsService.registerProductOptions(userId, false, command))
                .isSameAs(exception);

        verifyNoInteractions(saveProductOptionsPort);
    }

    @Test
    @DisplayName("상품 옵션 등록 시 저장에 실패하면 예외를 전파한다")
    void register_saveFails_propagates() {
        Long userId = 7L;
        Long productId = 70L;
        RegisterProductOptionsCommand command = buildCommand(productId, List.of("옵션1"));
        Product product = buildProduct(productId, userId);
        RuntimeException exception = new RuntimeException("save fail");

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(saveProductOptionsPort.save(any(ProductOptionCategory.class))).thenThrow(exception);

        assertThatThrownBy(() -> registerProductOptionsService.registerProductOptions(userId, false, command))
                .isSameAs(exception);
    }

    private RegisterProductOptionsCommand buildCommand(Long productId, List<String> optionContents) {
        List<RegisterProductOptionsCommand.OptionItem> options = optionContents
                .stream()
                .map(RegisterProductOptionsCommand.OptionItem::new)
                .toList();
        return RegisterProductOptionsCommand.of(productId, "옵션", options);
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
