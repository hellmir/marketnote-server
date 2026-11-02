package com.personal.marketnote.product.domain.category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    @DisplayName("CreateState로 카테고리를 생성하면 기본 상태는 ACTIVE이다")
    void shouldCreateCategoryWithActiveStatus() {
        // given
        CategoryCreateState state = CategoryCreateState.of(1L, "전자제품");

        // when
        Category category = Category.from(state);

        // then
        assertThat(category.getParentCategoryId()).isEqualTo(1L);
        assertThat(category.getName()).isEqualTo("전자제품");
        assertThat(category.isActive()).isTrue();
    }

    @Test
    @DisplayName("delete 호출 시 상태가 INACTIVE로 변경된다")
    void shouldChangeStatusToInactiveWhenDeleted() {
        // given
        Category category = Category.from(CategoryCreateState.of(1L, "전자제품"));
        assertThat(category.isActive()).isTrue();

        // when
        category.delete();

        // then
        assertThat(category.isInactive()).isTrue();
        assertThat(category.isActive()).isFalse();
    }
}
