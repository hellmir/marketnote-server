package com.personal.marketnote.commerce.adapter.in.web.order.request;

import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CancelOrderRequest {
    @Schema(
            name = "reasonCategory",
            description = "취소 사유 카테고리",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OrderStatusReasonCategory reasonCategory;

    @Schema(
            name = "reason",
            description = "취소 사유",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 500, message = "취소 사유는 500자 이내여야 합니다.")
    private String reason;
}
