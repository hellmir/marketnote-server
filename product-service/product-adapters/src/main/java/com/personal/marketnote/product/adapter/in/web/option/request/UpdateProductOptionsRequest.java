package com.personal.marketnote.product.adapter.in.web.option.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateProductOptionsRequest {
    @Schema(name = "categoryName", description = "옵션 카테고리명", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "옵션 카테고리명은 필수입니다.")
    private String categoryName;

    @Schema(name = "options", description = "해당 카테고리에 등록할 옵션 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "옵션 목록은 필수입니다.")
    @Size(min = 1, message = "각 옵션 카테고리는 최소 1개 이상의 옵션을 가져야 합니다.")
    private List<@Valid RegisterProductOptionRequest> options;
}
