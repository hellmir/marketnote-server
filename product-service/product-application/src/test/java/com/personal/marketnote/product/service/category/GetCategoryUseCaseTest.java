package com.personal.marketnote.product.service.category;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.category.Category;
import com.personal.marketnote.product.domain.category.CategorySnapshotState;
import com.personal.marketnote.product.exception.CategoryNotFoundException;
import com.personal.marketnote.product.port.out.category.FindCategoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCategoryUseCaseTest {
    @Mock
    private FindCategoryPort findCategoryPort;

    @InjectMocks
    private GetCategoryService getCategoryService;

    @Test
    @DisplayName("카테고리 ID로 조회하면 카테고리를 반환한다")
    void getCategory_byId_returnsCategory() {
        Long categoryId = 10L;
        Category category = buildCategory(categoryId, null, "카테고리");

        when(findCategoryPort.findById(categoryId)).thenReturn(Optional.of(category));

        Category result = getCategoryService.getCategory(categoryId);

        assertThat(result).isSameAs(category);
        verify(findCategoryPort).findById(categoryId);
    }

    @Test
    @DisplayName("카테고리 ID로 조회 시 없으면 예외를 던진다")
    void getCategory_byId_notFound() {
        Long categoryId = 11L;

        when(findCategoryPort.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getCategoryService.getCategory(categoryId))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("카테고리 ID: 11");
    }

    @Test
    @DisplayName("카테고리 ID로 조회 시 포트 예외를 전파한다")
    void getCategory_byId_portFails_propagates() {
        Long categoryId = 12L;
        RuntimeException exception = new RuntimeException("find fail");

        when(findCategoryPort.findById(categoryId)).thenThrow(exception);

        assertThatThrownBy(() -> getCategoryService.getCategory(categoryId))
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
