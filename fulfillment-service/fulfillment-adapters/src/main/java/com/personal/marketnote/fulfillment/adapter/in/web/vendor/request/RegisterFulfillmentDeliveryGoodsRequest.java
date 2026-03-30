package com.personal.marketnote.fulfillment.adapter.in.web.vendor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RegisterFulfillmentDeliveryGoodsRequest {
    @Schema(
            name = "cstGodCd",
            description = "고객사상품번호",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "고객사상품번호는 필수값입니다.")
    private String cstGodCd;

    @Schema(
            name = "distTermDt",
            description = "유통기한",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String distTermDt;

    @Schema(
            name = "ordQty",
            description = "주문수량",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "주문수량은 필수값입니다.")
    private Integer ordQty;
}
