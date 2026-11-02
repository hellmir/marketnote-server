package com.personal.marketnote.commerce.adapter.in.web.order.request;

import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class ChangeOrderStatusRequest {
    @Schema(
            name = "pricePolicyIds",
            description = "가격 정책 ID 목록",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<Long> pricePolicyIds;

    @NotNull(message = "주문 상태는 필수값입니다.")
    @Schema(
            name = "orderStatus",
            description = "주문 상태",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OrderStatus orderStatus;

    @Schema(
            name = "reasonCategory",
            description = "변경 사유 카테고리",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OrderStatusReasonCategory reasonCategory;

    @Schema(
            name = "reason",
            description = "변경 사유",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 500, message = "변경 사유는 500자 이내여야 합니다.")
    private String reason;
}
