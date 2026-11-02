package com.personal.marketnote.product.adapter.in.web.cart.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateCartProductOptionsRequest(
        @Schema(description = "가격 정책 ID")
        @NotNull(message = "가격 정책 ID는 필수입니다.")
        @Min(value = 1, message = "가격 정책 ID는 1 이상이어야 합니다.")
        @Max(value = Long.MAX_VALUE, message = "가격 정책 ID는 정수형 최대값을 초과할 수 없습니다.")
        Long pricePolicyId,

        @Schema(description = "변경할 상품 옵션 ID 목록")
        @NotNull(message = "변경할 상품 옵션 ID 목록은 필수입니다.")
        List<Long> newOptionIds
) {
}
