package com.personal.marketnote.fulfillment.adapter.in.web.vendor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class CancelFulfillmentDeliveryRequest {
    @Schema(
            name = "slipNo",
            description = "출고요청번호",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "출고요청번호는 필수값입니다.")
    private String slipNo;

    @Schema(
            name = "ordNo",
            description = "주문번호",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "주문번호는 필수값입니다.")
    private String ordNo;
}
