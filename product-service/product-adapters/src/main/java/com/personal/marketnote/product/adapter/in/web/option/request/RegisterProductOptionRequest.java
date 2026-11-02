package com.personal.marketnote.product.adapter.in.web.option.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RegisterProductOptionRequest {
    @Schema(name = "content", description = "옵션 내용", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "옵션 내용은 필수입니다.")
    private String content;
}
