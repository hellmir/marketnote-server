package com.personal.marketnote.product.adapter.in.web.category.response;

import com.personal.marketnote.product.port.in.result.category.RegisterCategoryResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RegisterCategoryResponse {
    private Long id;
    private Long parentCategoryId;
    private String name;

    public static RegisterCategoryResponse from(RegisterCategoryResult result) {
        return RegisterCategoryResponse.builder()
                .id(result.id())
                .parentCategoryId(result.parentCategoryId())
                .name(result.name())
                .build();
    }
}


