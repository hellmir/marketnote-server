package com.personal.marketnote.product.adapter.in.web.option.response;

import com.personal.marketnote.product.port.in.result.option.ProductOptionCategoryItemResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductOptionCategoryResponse {
    private Long id;
    private String name;
    private Long orderNum;
    private String status;
    private List<ProductOptionResponse> options;

    public static ProductOptionCategoryResponse from(ProductOptionCategoryItemResult result) {
        return ProductOptionCategoryResponse.builder()
                .id(result.id())
                .name(result.name())
                .orderNum(result.orderNum())
                .status(result.status())
                .options(
                        result.options()
                                .stream()
                                .map(ProductOptionResponse::from)
                                .collect(Collectors.toList())
                )
                .build();
    }
}


