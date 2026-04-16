package com.personal.marketnote.commerce.adapter.in.web.quickpayment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ApproveQuickPaymentRequest {
    @Schema(
            name = "orderKey",
            description = "주문 키 (UUID)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "주문 키는 필수값입니다.")
    private String orderKey;

    @Schema(
            name = "quickPaymentCardId",
            description = "빠른결제 카드 ID",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "빠른결제 카드 ID는 필수값입니다.")
    @Min(value = 1, message = "빠른결제 카드 ID는 1 이상이어야 합니다.")
    private Long quickPaymentCardId;

    @Schema(
            name = "goodName",
            description = "상품명",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "상품명은 필수값입니다.")
    @Size(max = 100, message = "상품명은 100자를 초과할 수 없습니다.")
    private String goodName;
}
