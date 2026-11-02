package com.personal.marketnote.product.adapter.in.web.option.response;

import com.personal.marketnote.product.port.in.result.option.SelectableProductOptionCategoryItemResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class SelectableProductOptionCategoryResponse {
    private Long id;
    private String name;
    private Long orderNum;
    private String status;
    private List<SelectableProductOptionResponse> options;

    public static SelectableProductOptionCategoryResponse from(
            SelectableProductOptionCategoryItemResult category
    ) {
        return SelectableProductOptionCategoryResponse.builder()
                .id(category.id())
                .name(category.name())
                .orderNum(category.orderNum())
                .status(category.status())
                .options(
                        category.options().stream()
                                .map(SelectableProductOptionResponse::from)
                                .collect(Collectors.toList())
                )
                .build();
    }
}


