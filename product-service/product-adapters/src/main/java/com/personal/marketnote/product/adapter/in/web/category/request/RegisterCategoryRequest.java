package com.personal.marketnote.product.adapter.in.web.category.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RegisterCategoryRequest {
    @Schema(
            name = "parentCategoryId",
            description = "상위 카테고리 ID (없으면 루트로 등록)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "1"
    )
    private Long parentCategoryId;

    @Schema(
            name = "name",
            description = "카테고리명",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "루테인"
    )
    @NotBlank(message = "카테고리명은 필수입니다.")
    private String name;
}


