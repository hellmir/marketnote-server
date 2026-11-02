package com.personal.marketnote.fulfillment.adapter.in.web.vendor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class RegisterFulfillmentDeliveryRequest {
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
            name = "ordSeq",
            description = "FMS 요청번호 순번",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer ordSeq;

    @Schema(
            name = "slipNo",
            description = "FMS 출고요청번호(수정시 필수)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String slipNo;

    @Schema(
            name = "custNm",
            description = "배송 고객명(등록시 필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "배송 고객명은 필수값입니다.")
    private String custNm;

    @Schema(
            name = "custTelNo",
            description = "배송 고객번호(등록시 필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "배송 고객번호는 필수값입니다.")
    private String custTelNo;

    @Schema(
            name = "custAddr",
            description = "배송주소(등록시 필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "배송주소는 필수값입니다.")
    private String custAddr;

    @Schema(
            name = "outWay",
            description = "출고방법(1:선입선출,3:유통기한지정)(등록시 필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "출고방법은 필수값입니다.")
    private String outWay;

    @Schema(
            name = "sendNm",
            description = "보내는분",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String sendNm;

    @Schema(
            name = "sendTelNo",
            description = "보내는분전화번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String sendTelNo;

    @Schema(
            name = "salChanel",
            description = "판매채널",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String salChanel;

    @Schema(
            name = "shipReqTerm",
            description = "배송요청사항",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String shipReqTerm;

    @Schema(
            name = "godCds",
            description = "출고 상품 코드 목록",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Valid
    @NotEmpty(message = "출고 상품 목록은 필수값입니다.")
    private List<RegisterFulfillmentDeliveryGoodsRequest> godCds;

    @Schema(
            name = "oneDayDeliveryCd",
            description = "원데이배송회사 코드(NULL:사용안함, VROONG:부릉, CHAINLOGIS:체인로지스)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String oneDayDeliveryCd;

    @Schema(
            name = "remark",
            description = "비고(파스토에 요청하거나 공유할 내용)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String remark;
}
