package com.personal.marketnote.product.adapter.in.web.cart.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderingItemRequest(
        @Schema(description = "가격 정책 ID")
        @NotNull(message = "가격 정책 ID는 필수입니다.")
        @Min(value = 1, message = "가격 정책 ID는 1 이상이어야 합니다.")
        @Max(value = Long.MAX_VALUE, message = "가격 정책 ID는 정수형 최대값을 초과할 수 없습니다.")
        Long pricePolicyId,

        @Schema(
                name = "sharerKey",
                description = "링크 공유 회원 식별키",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        UUID sharerKey,

        @Schema(description = "상품 수량")
        @NotNull(message = "상품 수량은 필수입니다.")
        @Min(value = 1, message = "상품 수량은 1 이상이어야 합니다.")
        @Max(value = Short.MAX_VALUE, message = "상품 수량은 정수형 최대값을 초과할 수 없습니다.")
        Short quantity,

        @Schema(description = "상품 이미지 URL")
        @NotNull(message = "상품 이미지 URL은 필수입니다.")
        String imageUrl
) {
}
