package com.personal.marketnote.product.adapter.in.web.category.response;

import com.personal.marketnote.product.port.in.result.category.GetCategoriesResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class GetCategoriesResponse {
    private List<CategoryResponse> categories;

    public static GetCategoriesResponse from(GetCategoriesResult result) {
        return GetCategoriesResponse.builder()
                .categories(
                        result.categories()
                                .stream()
                                .map(CategoryResponse::from)
                                .collect(Collectors.toList())
                )
                .build();
    }
}


