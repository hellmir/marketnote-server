package com.personal.marketnote.commerce.adapter.in.web.payment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ReadyPaymentRequest {
    @Schema(name = "orderKey", description = "주문 키 (UUID)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String orderKey;

    @Schema(name = "payMethod", description = "결제 수단 (CARD, BANK_TRANSFER 등)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String payMethod;

    @Schema(name = "goodName", description = "상품명", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 100, message = "상품명은 100자 이내여야 합니다.")
    private String goodName;
}
