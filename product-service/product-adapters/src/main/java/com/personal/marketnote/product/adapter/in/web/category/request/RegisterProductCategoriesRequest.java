package com.personal.marketnote.product.adapter.in.web.category.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class RegisterProductCategoriesRequest {
    @Schema(
            name = "categoryIds",
            description = "등록할 카테고리 ID 목록",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "카테고리 ID 목록은 필수입니다.")
    private List<Long> categoryIds;
}
