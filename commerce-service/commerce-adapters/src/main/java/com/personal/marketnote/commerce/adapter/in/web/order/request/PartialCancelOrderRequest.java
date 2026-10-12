package com.personal.marketnote.commerce.adapter.in.web.order.request;

import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class PartialCancelOrderRequest {
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

    @Schema(
            name = "cancelPricePolicyIds",
            description = "취소 대상 상품의 가격 정책 ID 목록",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "취소 대상 상품은 1개 이상이어야 합니다.")
    @Size(max = 100, message = "취소 대상 상품은 100개를 초과할 수 없습니다.")
    private List<Long> cancelPricePolicyIds;
}
