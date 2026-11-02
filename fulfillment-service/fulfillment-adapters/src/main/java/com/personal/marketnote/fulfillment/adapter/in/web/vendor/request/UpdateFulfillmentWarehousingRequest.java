package com.personal.marketnote.fulfillment.adapter.in.web.vendor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateFulfillmentWarehousingRequest {
    @Schema(
            name = "ordDt",
            description = "입고 요청일",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "입고 요청일은 필수값입니다.")
    private String ordDt;

    @Schema(
            name = "ordNo",
            description = "주문번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String ordNo;

    @Schema(
            name = "inWay",
            description = "입고방법(01:택배,02:차량)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "입고방법은 필수값입니다.")
    private String inWay;

    @Schema(
            name = "slipNo",
            description = "입고 요청번호(수정시 필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "입고 요청번호는 필수값입니다.")
    private String slipNo;

    @Schema(
            name = "parcelComp",
            description = "택배사명",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String parcelComp;

    @Schema(
            name = "parcelInvoiceNo",
            description = "송장번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String parcelInvoiceNo;

    @Schema(
            name = "remark",
            description = "입고시 참고사항",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String remark;

    @Schema(
            name = "cstSupCd",
            description = "고객사공급사코드",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String cstSupCd;

    @Schema(
            name = "distTermDt",
            description = "유통기한지정일 (YYYY-MM-DD)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String distTermDt;

    @Schema(
            name = "makeDt",
            description = "제조일자 (YYYY-MM-DD)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String makeDt;

    @Schema(
            name = "preArv",
            description = "사전도착여부",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String preArv;

    @Schema(
            name = "godCds",
            description = "입고 상품 코드 목록",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Valid
    @NotEmpty(message = "입고 상품 목록은 필수값입니다.")
    private List<UpdateFulfillmentWarehousingGoodsRequest> godCds;
}
