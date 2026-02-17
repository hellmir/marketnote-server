package com.personal.marketnote.product.service.category;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.category.Category;
import com.personal.marketnote.product.domain.category.CategorySnapshotState;
import com.personal.marketnote.product.port.in.result.category.CategoryItemResult;
import com.personal.marketnote.product.port.in.result.category.GetCategoriesResult;
import com.personal.marketnote.product.port.out.category.FindCategoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCategoriesByParentIdUseCaseTest {
    @Mock
    private FindCategoryPort findCategoryPort;

    @InjectMocks
    private GetCategoryService getCategoryService;

    @Test
    @DisplayName("상위 카테고리 ID로 조회하면 카테고리 목록을 매핑해 반환한다")
    void getCategoriesByParentId_mapsCategories() {
        Long parentId = 20L;
        Category category1 = buildCategory(21L, parentId, "카테고리1", EntityStatus.ACTIVE);
        Category category2 = buildCategory(22L, parentId, "카테고리2", EntityStatus.UNEXPOSED);

        when(findCategoryPort.findActiveByParentId(parentId)).thenReturn(List.of(category1, category2));

        GetCategoriesResult result = getCategoryService.getCategoriesByParentId(parentId);

        assertThat(result.categories()).hasSize(2);
        CategoryItemResult item1 = result.categories().getFirst();
        CategoryItemResult item2 = result.categories().getLast();

        assertThat(item1.id()).isEqualTo(21L);
        assertThat(item1.parentCategoryId()).isEqualTo(parentId);
        assertThat(item1.name()).isEqualTo("카테고리1");
        assertThat(item1.status()).isEqualTo(EntityStatus.ACTIVE.name());

        assertThat(item2.id()).isEqualTo(22L);
        assertThat(item2.parentCategoryId()).isEqualTo(parentId);
        assertThat(item2.name()).isEqualTo("카테고리2");
        assertThat(item2.status()).isEqualTo(EntityStatus.UNEXPOSED.name());
    }

    @Test
    @DisplayName("상위 카테고리 ID로 조회 시 카테고리가 없으면 빈 목록을 반환한다")
    void getCategoriesByParentId_empty() {
        Long parentId = 21L;

        when(findCategoryPort.findActiveByParentId(parentId)).thenReturn(List.of());

        GetCategoriesResult result = getCategoryService.getCategoriesByParentId(parentId);

        assertThat(result.categories()).isEmpty();
        verify(findCategoryPort).findActiveByParentId(parentId);
    }

    @Test
    @DisplayName("상위 카테고리 ID로 조회 시 포트 예외를 전파한다")
    void getCategoriesByParentId_portFails_propagates() {
        Long parentId = 22L;
        RuntimeException exception = new RuntimeException("find fail");

        when(findCategoryPort.findActiveByParentId(parentId)).thenThrow(exception);

        assertThatThrownBy(() -> getCategoryService.getCategoriesByParentId(parentId))
                .isSameAs(exception);
    }

    private Category buildCategory(Long id, Long parentId, String name, EntityStatus status) {
        return Category.from(
                CategorySnapshotState.builder()
                        .id(id)
                        .parentCategoryId(parentId)
                        .name(name)
                        .status(status)
                        .build()
        );
    }
}
