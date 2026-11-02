package com.personal.marketnote.commerce.adapter.in.web.order.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.UUID;

@Getter
public class OrderProductItemRequest {
    @Schema(
            name = "productId",
            description = "상품 ID",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "상품 ID는 필수값입니다.")
    @Min(value = 1, message = "상품 ID는 1 이상이어야 합니다.")
    @Max(value = Long.MAX_VALUE, message = "상품 ID는 정수형 최대값을 초과할 수 없습니다.")
    private Long productId;

    @Schema(
            name = "sellerId",
            description = "판매자 회원 ID",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "판매자 회원 ID는 필수값입니다.")
    @Min(value = 1, message = "판매자 회원 ID는 1 이상이어야 합니다.")
    @Max(value = Long.MAX_VALUE, message = "판매자 회원 ID는 정수형 최대값을 초과할 수 없습니다.")
    private Long sellerId;

    @Schema(
            name = "pricePolicyId",
            description = "가격 정책 ID",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "가격 정책 ID는 필수값입니다.")
    @Min(value = 1, message = "가격 정책 ID는 1 이상이어야 합니다.")
    @Max(value = Long.MAX_VALUE, message = "가격 정책 ID는 정수형 최대값을 초과할 수 없습니다.")
    private Long pricePolicyId;

    @Schema(
            name = "sharerKey",
            description = "링크 공유 회원 식별키",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private UUID sharerKey;

    @Schema(
            name = "quantity",
            description = "주문 수량",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "주문 수량은 필수값입니다.")
    @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다.")
    private Integer quantity;

    @Schema(
            name = "unitAmount",
            description = "단가(원)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "단가는 필수값입니다.")
    @Min(value = 0, message = "단가는 0 이상이어야 합니다.")
    private Long unitAmount;

    @Schema(
            name = "imageUrl",
            description = "상품 이미지 URL",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 2048, message = "이미지 URL은 2048자 이내여야 합니다.")
    private String imageUrl;
}
