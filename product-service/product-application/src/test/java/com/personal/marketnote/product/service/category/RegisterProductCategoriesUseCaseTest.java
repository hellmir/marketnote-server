package com.personal.marketnote.product.service.category;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.category.Category;
import com.personal.marketnote.product.domain.category.CategorySnapshotState;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.exception.InvalidProductCategoryException;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.port.in.command.RegisterProductCategoriesCommand;
import com.personal.marketnote.product.port.in.result.category.RegisterProductCategoriesResult;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.category.FindCategoryPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productcategory.ReplaceProductCategoriesPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterProductCategoriesUseCaseTest {
    @Mock
    private GetProductUseCase getProductUseCase;
    @Mock
    private FindProductPort findProductPort;
    @Mock
    private FindCategoryPort findCategoryPort;
    @Mock
    private ReplaceProductCategoriesPort replaceProductCategoriesPort;

    @InjectMocks
    private RegisterProductCategoriesService registerProductCategoriesService;

    @Test
    @DisplayName("상품 카테고리 등록 요청 회원이 관리자 또는 상품 판매자가 아니면 예외를 던진다")
    void registerProductCategories_notOwner_throws() {
        Long userId = 1L;
        Long productId = 10L;
        RegisterProductCategoriesCommand command = RegisterProductCategoriesCommand.of(productId, List.of(1L, 2L));
        Product product = buildProduct(productId, 999L);

        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(false);

        assertThatThrownBy(() -> registerProductCategoriesService.registerProductCategories(userId, false, command))
                .isInstanceOf(NotProductOwnerException.class)
                .hasMessageContaining("관리자 또는 상품 판매자가 아닙니다");

        verify(getProductUseCase).getProduct(productId);
        verify(findProductPort).existsByIdAndSellerId(productId, userId);
        verifyNoInteractions(findCategoryPort, replaceProductCategoriesPort);
    }

    @Test
    @DisplayName("상품 카테고리 등록 시 카테고리가 누락되면 예외를 던진다")
    void registerProductCategories_missingCategory_throws() {
        Long userId = 2L;
        Long productId = 20L;
        List<Long> categoryIds = List.of(1L, 2L, 3L);
        RegisterProductCategoriesCommand command = RegisterProductCategoriesCommand.of(productId, categoryIds);
        Product product = buildProduct(productId, userId);

        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(findCategoryPort.findAllActiveByIds(categoryIds))
                .thenReturn(List.of(buildCategory(1L, "카테고리1"), buildCategory(2L, "카테고리2")));

        assertThatThrownBy(() -> registerProductCategoriesService.registerProductCategories(userId, false, command))
                .isInstanceOf(InvalidProductCategoryException.class)
                .hasMessageContaining("ERR_PRODUCT_CATEGORY_01");

        verify(replaceProductCategoriesPort, never()).replaceProductCategories(anyLong(), any());
    }

    @Test
    @DisplayName("상품 카테고리 등록 시 관리자 요청이면 소유자 확인 없이 등록한다")
    void registerProductCategories_admin_skipsOwnerCheck() {
        Long userId = 3L;
        Long productId = 30L;
        List<Long> categoryIds = List.of(10L, 20L);
        RegisterProductCategoriesCommand command = RegisterProductCategoriesCommand.of(productId, categoryIds);
        Product product = buildProduct(productId, 999L);

        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(findCategoryPort.findAllActiveByIds(categoryIds))
                .thenReturn(List.of(buildCategory(10L, "카테고리10"), buildCategory(20L, "카테고리20")));

        RegisterProductCategoriesResult result = registerProductCategoriesService.registerProductCategories(
                userId, true, command
        );

        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.categoryIds()).containsExactlyElementsOf(categoryIds);
        verifyNoInteractions(findProductPort);
        verify(replaceProductCategoriesPort).replaceProductCategories(productId, categoryIds);
    }

    @Test
    @DisplayName("상품 카테고리 등록 시 카테고리 목록이 비어 있어도 등록한다")
    void registerProductCategories_emptyCategoryIds_registers() {
        Long userId = 4L;
        Long productId = 40L;
        List<Long> categoryIds = List.of();
        RegisterProductCategoriesCommand command = RegisterProductCategoriesCommand.of(productId, categoryIds);
        Product product = buildProduct(productId, userId);

        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(findCategoryPort.findAllActiveByIds(categoryIds)).thenReturn(List.of());

        RegisterProductCategoriesResult result = registerProductCategoriesService.registerProductCategories(
                userId, false, command
        );

        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.categoryIds()).isEmpty();
        verify(replaceProductCategoriesPort).replaceProductCategories(productId, categoryIds);
    }

    @Test
    @DisplayName("상품 카테고리 등록 시 상품 조회에 실패하면 예외를 전파한다")
    void registerProductCategories_getProductFails_propagates() {
        Long userId = 5L;
        Long productId = 50L;
        RegisterProductCategoriesCommand command = RegisterProductCategoriesCommand.of(productId, List.of(1L));
        RuntimeException exception = new RuntimeException("product fail");

        when(getProductUseCase.getProduct(productId)).thenThrow(exception);

        assertThatThrownBy(() -> registerProductCategoriesService.registerProductCategories(userId, false, command))
                .isSameAs(exception);

        verifyNoInteractions(findProductPort, findCategoryPort, replaceProductCategoriesPort);
    }

    @Test
    @DisplayName("상품 카테고리 등록 시 카테고리 조회에 실패하면 예외를 전파한다")
    void registerProductCategories_findCategoryFails_propagates() {
        Long userId = 6L;
        Long productId = 60L;
        List<Long> categoryIds = List.of(1L);
        RegisterProductCategoriesCommand command = RegisterProductCategoriesCommand.of(productId, categoryIds);
        Product product = buildProduct(productId, userId);
        RuntimeException exception = new RuntimeException("category fail");

        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(findCategoryPort.findAllActiveByIds(categoryIds)).thenThrow(exception);

        assertThatThrownBy(() -> registerProductCategoriesService.registerProductCategories(userId, false, command))
                .isSameAs(exception);

        verifyNoInteractions(replaceProductCategoriesPort);
    }

    @Test
    @DisplayName("상품 카테고리 등록 시 저장에 실패하면 예외를 전파한다")
    void registerProductCategories_replaceFails_propagates() {
        Long userId = 7L;
        Long productId = 70L;
        List<Long> categoryIds = List.of(1L, 2L);
        RegisterProductCategoriesCommand command = RegisterProductCategoriesCommand.of(productId, categoryIds);
        Product product = buildProduct(productId, userId);
        RuntimeException exception = new RuntimeException("replace fail");

        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(findCategoryPort.findAllActiveByIds(categoryIds))
                .thenReturn(List.of(buildCategory(1L, "카테고리1"), buildCategory(2L, "카테고리2")));
        doThrow(exception).when(replaceProductCategoriesPort).replaceProductCategories(productId, categoryIds);

        assertThatThrownBy(() -> registerProductCategoriesService.registerProductCategories(userId, false, command))
                .isSameAs(exception);
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

    private Category buildCategory(Long id, String name) {
        return Category.from(
                CategorySnapshotState.builder()
                        .id(id)
                        .name(name)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
