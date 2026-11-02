package com.personal.marketnote.product.adapter.in.web.category.response;

import com.personal.marketnote.product.port.in.result.category.CategoryItemResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class CategoryResponse {
    private Long id;
    private Long parentCategoryId;
    private String name;
    private String status;

    public static CategoryResponse from(CategoryItemResult result) {
        return CategoryResponse.builder()
                .id(result.id())
                .parentCategoryId(result.parentCategoryId())
                .name(result.name())
                .status(result.status())
                .build();
    }
}


