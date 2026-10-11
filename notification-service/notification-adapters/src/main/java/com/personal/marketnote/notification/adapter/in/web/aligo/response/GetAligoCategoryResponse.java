package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.port.in.result.aligo.GetAligoCategoryTreeResult;

import java.util.List;

public record GetAligoCategoryResponse(
        List<CategoryResponse> categories
) {

    public record CategoryResponse(
            String code,
            String name,
            List<CategoryResponse> children
    ) {
    }

    public static GetAligoCategoryResponse from(GetAligoCategoryTreeResult result) {
        List<CategoryResponse> categories = result.categories().stream()
                .map(GetAligoCategoryResponse::mapCategory)
                .toList();
        return new GetAligoCategoryResponse(categories);
    }

    private static CategoryResponse mapCategory(GetAligoCategoryTreeResult.CategoryResult category) {
        List<CategoryResponse> children = category.children().stream()
                .map(GetAligoCategoryResponse::mapCategory)
                .toList();
        return new CategoryResponse(category.code(), category.name(), children);
    }
}
