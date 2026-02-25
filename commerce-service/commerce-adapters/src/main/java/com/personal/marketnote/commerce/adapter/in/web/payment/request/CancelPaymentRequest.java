package com.personal.marketnote.commerce.adapter.in.web.payment.request;

import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CancelPaymentRequest {
    @Schema(name = "cancelType", description = "취소 유형 (FULL: 전체취소, PARTIAL: 부분취소)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private CancelPaymentCommand.CancelType cancelType;

    @Schema(name = "cancelAmount", description = "취소 금액 (부분취소 시 필수)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long cancelAmount;

    @Schema(name = "cancelReason", description = "취소 사유", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String cancelReason;
}
