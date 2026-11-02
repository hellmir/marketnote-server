package com.personal.marketnote.product.adapter.in.web.category.response;

import com.personal.marketnote.product.port.in.result.category.RegisterProductCategoriesResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RegisterProductCategoriesResponse {
    private Long productId;
    private List<Long> categoryIds;

    public static RegisterProductCategoriesResponse from(RegisterProductCategoriesResult result) {
        return RegisterProductCategoriesResponse.builder()
                .productId(result.productId())
                .categoryIds(result.categoryIds())
                .build();
    }
}
