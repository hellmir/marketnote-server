package com.personal.marketnote.commerce.adapter.in.web.payment.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApprovePaymentRequest {
    @Schema(name = "orderKey", description = "주문 키 (UUID)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String orderKey;

    @Schema(name = "encData", description = "KCP 결제창 인증결과 암호화 데이터", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String encData;

    @Schema(name = "encInfo", description = "KCP 결제창 인증결과 암호화 정보", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String encInfo;

    @Schema(name = "payType", description = "결제수단 (PACA: 신용카드, PABK: 계좌이체, PAMC: 휴대폰, PAPT: 포인트, PATK: 상품권)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String payType;
}
