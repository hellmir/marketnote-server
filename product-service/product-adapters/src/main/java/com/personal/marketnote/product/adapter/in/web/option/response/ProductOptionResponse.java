package com.personal.marketnote.product.adapter.in.web.option.response;

import com.personal.marketnote.product.port.in.result.option.ProductOptionItemResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductOptionResponse {
    private Long id;
    private String content;
    private String status;

    public static ProductOptionResponse from(ProductOptionItemResult result) {
        return ProductOptionResponse.builder()
                .id(result.id())
                .content(result.content())
                .status(result.status())
                .build();
    }
}
