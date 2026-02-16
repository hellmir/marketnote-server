package com.personal.marketnote.product.service.category;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.category.Category;
import com.personal.marketnote.product.domain.category.CategorySnapshotState;
import com.personal.marketnote.product.exception.CategoryNotFoundException;
import com.personal.marketnote.product.port.in.command.RegisterCategoryCommand;
import com.personal.marketnote.product.port.in.result.category.RegisterCategoryResult;
import com.personal.marketnote.product.port.out.category.FindCategoryPort;
import com.personal.marketnote.product.port.out.category.SaveCategoryPort;
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
class RegisterCategoryUseCaseTest {
    @Mock
    private SaveCategoryPort saveCategoryPort;
    @Mock
    private FindCategoryPort findCategoryPort;

    @InjectMocks
    private RegisterCategoryService registerCategoryService;

    @Test
    @DisplayName("카테고리 등록 시 상위 카테고리가 없으면 예외를 던진다")
    void registerCategory_parentNotFound_throws() {
        RegisterCategoryCommand command = RegisterCategoryCommand.of(10L, "카테고리");

        when(findCategoryPort.findAllActiveByIds(List.of(10L))).thenReturn(List.of());

        assertThatThrownBy(() -> registerCategoryService.registerCategory(command))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("상위 카테고리 ID: 10");

        verify(findCategoryPort).findAllActiveByIds(List.of(10L));
        verifyNoInteractions(saveCategoryPort);
    }

    @Test
    @DisplayName("카테고리 등록 시 상위 카테고리가 존재하면 저장 후 결과를 반환한다")
    void registerCategory_withParent_savesAndReturns() {
        RegisterCategoryCommand command = RegisterCategoryCommand.of(11L, "카테고리");
        Category parent = buildCategory(11L, null, "상위");
        Category saved = buildCategory(100L, 11L, "카테고리");

        when(findCategoryPort.findAllActiveByIds(List.of(11L))).thenReturn(List.of(parent));
        when(saveCategoryPort.save(any(Category.class))).thenReturn(saved);

        RegisterCategoryResult result = registerCategoryService.registerCategory(command);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.parentCategoryId()).isEqualTo(11L);
        assertThat(result.name()).isEqualTo("카테고리");

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(saveCategoryPort).save(captor.capture());
        Category requestCategory = captor.getValue();
        assertThat(requestCategory.getParentCategoryId()).isEqualTo(11L);
        assertThat(requestCategory.getName()).isEqualTo("카테고리");
    }

    @Test
    @DisplayName("카테고리 등록 시 상위 카테고리 ID가 없으면 검증 없이 저장한다")
    void registerCategory_withoutParent_skipsFind() {
        RegisterCategoryCommand command = RegisterCategoryCommand.of(null, "카테고리");
        Category saved = buildCategory(200L, null, "카테고리");

        when(saveCategoryPort.save(any(Category.class))).thenReturn(saved);

        RegisterCategoryResult result = registerCategoryService.registerCategory(command);

        assertThat(result.id()).isEqualTo(200L);
        assertThat(result.parentCategoryId()).isNull();
        assertThat(result.name()).isEqualTo("카테고리");

        verifyNoInteractions(findCategoryPort);
    }

    @Test
    @DisplayName("카테고리 등록 시 상위 카테고리 ID가 -1이면 검증 없이 저장한다")
    void registerCategory_parentMinusOne_skipsFind() {
        RegisterCategoryCommand command = RegisterCategoryCommand.of(-1L, "카테고리");
        Category saved = buildCategory(300L, -1L, "카테고리");

        when(saveCategoryPort.save(any(Category.class))).thenReturn(saved);

        RegisterCategoryResult result = registerCategoryService.registerCategory(command);

        assertThat(result.id()).isEqualTo(300L);
        assertThat(result.parentCategoryId()).isEqualTo(-1L);
        assertThat(result.name()).isEqualTo("카테고리");

        verifyNoInteractions(findCategoryPort);
    }

    @Test
    @DisplayName("카테고리 등록 시 상위 카테고리 조회에 실패하면 예외를 전파한다")
    void registerCategory_findParentFails_propagates() {
        RegisterCategoryCommand command = RegisterCategoryCommand.of(12L, "카테고리");
        RuntimeException exception = new RuntimeException("find fail");

        when(findCategoryPort.findAllActiveByIds(List.of(12L))).thenThrow(exception);

        assertThatThrownBy(() -> registerCategoryService.registerCategory(command))
                .isSameAs(exception);

        verifyNoInteractions(saveCategoryPort);
    }

    @Test
    @DisplayName("카테고리 등록 시 저장에 실패하면 예외를 전파한다")
    void registerCategory_saveFails_propagates() {
        RegisterCategoryCommand command = RegisterCategoryCommand.of(13L, "카테고리");
        Category parent = buildCategory(13L, null, "상위");
        RuntimeException exception = new RuntimeException("save fail");

        when(findCategoryPort.findAllActiveByIds(List.of(13L))).thenReturn(List.of(parent));
        when(saveCategoryPort.save(any(Category.class))).thenThrow(exception);

        assertThatThrownBy(() -> registerCategoryService.registerCategory(command))
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
