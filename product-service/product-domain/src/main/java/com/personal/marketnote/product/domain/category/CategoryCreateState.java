package com.personal.marketnote.product.domain.category;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryCreateState {
    private final Long parentCategoryId;
    private final String name;

    public static CategoryCreateState of(Long parentCategoryId, String name) {
        return new CategoryCreateState(parentCategoryId, name);
    }
}

