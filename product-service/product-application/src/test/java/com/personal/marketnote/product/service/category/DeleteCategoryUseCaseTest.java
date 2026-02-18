package com.personal.marketnote.product.service.category;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.category.Category;
import com.personal.marketnote.product.domain.category.CategorySnapshotState;
import com.personal.marketnote.product.exception.CategoryHasChildrenException;
import com.personal.marketnote.product.exception.CategoryHasProductsException;
import com.personal.marketnote.product.exception.CategoryNotFoundException;
import com.personal.marketnote.product.port.in.command.DeleteCategoryCommand;
import com.personal.marketnote.product.port.in.usecase.category.GetCategoryUseCase;
import com.personal.marketnote.product.port.out.category.FindCategoryPort;
import com.personal.marketnote.product.port.out.category.UpdateCategoryPort;
import com.personal.marketnote.product.port.out.productcategory.FindProductCategoryPort;
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
class DeleteCategoryUseCaseTest {
    @Mock
    private GetCategoryUseCase getCategoryUseCase;
    @Mock
    private FindCategoryPort findCategoryPort;
    @Mock
    private UpdateCategoryPort updateCategoryPort;
    @Mock
    private FindProductCategoryPort findProductCategoryPort;

    @InjectMocks
    private DeleteCategoryService deleteCategoryService;

    @Test
    @DisplayName("카테고리 삭제 시 카테고리가 존재하지 않으면 예외를 던진다")
    void deleteCategory_notFound_throws() {
        DeleteCategoryCommand command = DeleteCategoryCommand.of(10L);

        when(findCategoryPort.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> deleteCategoryService.deleteCategory(command))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("카테고리 ID: 10");

        verify(findCategoryPort).existsById(10L);
        verify(findCategoryPort, never()).existsChildren(anyLong());
        verifyNoInteractions(findProductCategoryPort, getCategoryUseCase, updateCategoryPort);
    }

    @Test
    @DisplayName("카테고리 삭제 시 하위 카테고리가 있으면 예외를 던진다")
    void deleteCategory_hasChildren_throws() {
        DeleteCategoryCommand command = DeleteCategoryCommand.of(11L);

        when(findCategoryPort.existsById(11L)).thenReturn(true);
        when(findCategoryPort.existsChildren(11L)).thenReturn(true);

        assertThatThrownBy(() -> deleteCategoryService.deleteCategory(command))
                .isInstanceOf(CategoryHasChildrenException.class)
                .hasMessageContaining("하위 카테고리");

        verify(findCategoryPort).existsChildren(11L);
        verifyNoInteractions(findProductCategoryPort, getCategoryUseCase, updateCategoryPort);
    }

    @Test
    @DisplayName("카테고리 삭제 시 등록된 상품이 있으면 예외를 던진다")
    void deleteCategory_hasProducts_throws() {
        DeleteCategoryCommand command = DeleteCategoryCommand.of(12L);

        when(findCategoryPort.existsById(12L)).thenReturn(true);
        when(findCategoryPort.existsChildren(12L)).thenReturn(false);
        when(findProductCategoryPort.existsByCategoryId(12L)).thenReturn(true);

        assertThatThrownBy(() -> deleteCategoryService.deleteCategory(command))
                .isInstanceOf(CategoryHasProductsException.class)
                .hasMessageContaining("등록된 상품");

        verify(findProductCategoryPort).existsByCategoryId(12L);
        verifyNoInteractions(getCategoryUseCase, updateCategoryPort);
    }

    @Test
    @DisplayName("카테고리 삭제 시 카테고리를 비활성화하고 저장한다")
    void deleteCategory_success_deactivatesAndUpdates() {
        DeleteCategoryCommand command = DeleteCategoryCommand.of(13L);
        Category category = buildCategory(13L, null, "카테고리");

        when(findCategoryPort.existsById(13L)).thenReturn(true);
        when(findCategoryPort.existsChildren(13L)).thenReturn(false);
        when(findProductCategoryPort.existsByCategoryId(13L)).thenReturn(false);
        when(getCategoryUseCase.getCategory(13L)).thenReturn(category);

        deleteCategoryService.deleteCategory(command);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(updateCategoryPort).update(captor.capture());
        Category updated = captor.getValue();

        assertThat(updated).isSameAs(category);
        assertThat(updated.isInactive()).isTrue();
    }

    @Test
    @DisplayName("카테고리 삭제 시 카테고리 조회에 실패하면 예외를 전파한다")
    void deleteCategory_getCategoryFails_propagates() {
        DeleteCategoryCommand command = DeleteCategoryCommand.of(14L);
        RuntimeException exception = new RuntimeException("get fail");

        when(findCategoryPort.existsById(14L)).thenReturn(true);
        when(findCategoryPort.existsChildren(14L)).thenReturn(false);
        when(findProductCategoryPort.existsByCategoryId(14L)).thenReturn(false);
        when(getCategoryUseCase.getCategory(14L)).thenThrow(exception);

        assertThatThrownBy(() -> deleteCategoryService.deleteCategory(command))
                .isSameAs(exception);

        verifyNoInteractions(updateCategoryPort);
    }

    @Test
    @DisplayName("카테고리 삭제 시 저장에 실패하면 예외를 전파한다")
    void deleteCategory_updateFails_propagates() {
        DeleteCategoryCommand command = DeleteCategoryCommand.of(15L);
        Category category = buildCategory(15L, null, "카테고리");
        RuntimeException exception = new RuntimeException("update fail");

        when(findCategoryPort.existsById(15L)).thenReturn(true);
        when(findCategoryPort.existsChildren(15L)).thenReturn(false);
        when(findProductCategoryPort.existsByCategoryId(15L)).thenReturn(false);
        when(getCategoryUseCase.getCategory(15L)).thenReturn(category);
        doThrow(exception).when(updateCategoryPort).update(category);

        assertThatThrownBy(() -> deleteCategoryService.deleteCategory(command))
                .isSameAs(exception);
    }

    private Category buildCategory(Long id, Long parentId, String name) {
        return Category.from(
                CategorySnapshotState.builder()
                        .id(id)
                        .parentCategoryId(parentId)
                        .name(name)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
