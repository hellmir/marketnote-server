package com.personal.marketnote.product.domain.category;

import com.personal.marketnote.common.domain.BaseDomain;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Category extends BaseDomain {
    private Long id;
    private Long parentCategoryId;
    private String name;

    public static Category from(CategoryCreateState state) {
        return Category.builder()
                .parentCategoryId(state.getParentCategoryId())
                .name(state.getName())
                .build();
    }

    public static Category from(CategorySnapshotState state) {
        Category category = Category.builder()
                .id(state.getId())
                .parentCategoryId(state.getParentCategoryId())
                .name(state.getName())
                .build();
        category.status = state.getStatus();

        return category;
    }

    public void delete() {
        deactivate();
    }
}
