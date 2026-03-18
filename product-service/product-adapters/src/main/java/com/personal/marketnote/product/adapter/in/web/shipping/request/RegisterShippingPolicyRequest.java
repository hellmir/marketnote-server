package com.personal.marketnote.product.adapter.in.web.shipping.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterShippingPolicyRequest(
        @NotBlank(message = "배송업체는 필수입니다")
        @Size(max = 50)
        @Schema(description = "배송업체", example = "한진택배")
        String deliveryCompany,

        @NotNull(message = "배송비는 필수입니다")
        @Min(value = 0, message = "배송비는 0 이상이어야 합니다")
        @Schema(description = "배송비", example = "3000")
        Long shippingFee,

        @NotNull(message = "무료배송 기준금액은 필수입니다")
        @Min(value = 0, message = "무료배송 기준금액은 0 이상이어야 합니다")
        @Schema(description = "무료배송 기준금액", example = "20000")
        Long freeShippingThreshold
) {
}
