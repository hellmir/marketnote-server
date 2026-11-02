package com.personal.marketnote.product.adapter.in.web.option.response;

import com.personal.marketnote.product.port.in.result.option.GetProductOptionsResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class GetProductOptionsResponse {
    private List<ProductOptionCategoryResponse> categories;

    public static GetProductOptionsResponse from(GetProductOptionsResult result) {
        return GetProductOptionsResponse.builder()
                .categories(
                        result.categories()
                                .stream()
                                .map(ProductOptionCategoryResponse::from)
                                .collect(Collectors.toList())
                )
                .build();
    }
}


