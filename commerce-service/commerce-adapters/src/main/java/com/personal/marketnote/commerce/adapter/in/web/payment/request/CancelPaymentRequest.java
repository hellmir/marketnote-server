package com.personal.marketnote.commerce.adapter.in.web.payment.request;

import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class CancelPaymentRequest {
    @Schema(name = "cancelType", description = "취소 유형 (FULL: 전체취소, PARTIAL: 부분취소)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private CancelPaymentCommand.CancelType cancelType;

    @Schema(name = "cancelAmount", description = "취소 금액 (부분취소 시 필수)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long cancelAmount;

    @Schema(name = "cancelReason", description = "취소 사유", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String cancelReason;

    @Schema(name = "cancelProducts", description = "취소 대상 상품 목록 (부분취소 시 재고 복구용, 선택)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Valid
    private List<CancelProductItemRequest> cancelProducts;

    @Getter
    public static class CancelProductItemRequest {
        @Schema(description = "가격 정책 ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "가격 정책 ID는 필수입니다")
        @Min(1)
        private Long pricePolicyId;

        @Schema(description = "취소 수량", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "취소 수량은 필수입니다")
        @Min(1)
        private Integer quantity;
    }
}

