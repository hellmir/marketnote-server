package com.personal.marketnote.fulfillment.adapter.in.web.vendor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateFulfillmentDeliveryCarRequest {
    @Schema(
            name = "ordDt",
            description = "요청일자(등록/수정시 필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "요청일자는 필수값입니다.")
    private String ordDt;

    @Schema(
            name = "ordNo",
            description = "주문번호(등록/수정시 필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "주문번호는 필수값입니다.")
    private String ordNo;

    @Schema(
            name = "slipNo",
            description = "FMS 출고요청번호(수정시 필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "출고요청번호는 필수값입니다.")
    private String slipNo;

    @Schema(
            name = "outWay",
            description = "출고방법(1:선입선출,3:유통기한지정)(등록시 필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "출고방법은 필수값입니다.")
    private String outWay;

    @Schema(
            name = "cstShopCd",
            description = "고객사출고처코드(등록시 필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "고객사출고처코드는 필수값입니다.")
    private String cstShopCd;

    @Schema(
            name = "godCds",
            description = "출고 상품 코드 목록",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Valid
    @NotEmpty(message = "출고 상품 목록은 필수값입니다.")
    private List<RegisterFulfillmentDeliveryGoodsRequest> godCds;

    @Schema(
            name = "remark",
            description = "비고(파스토에 요청하거나 공유할 내용)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String remark;
}
