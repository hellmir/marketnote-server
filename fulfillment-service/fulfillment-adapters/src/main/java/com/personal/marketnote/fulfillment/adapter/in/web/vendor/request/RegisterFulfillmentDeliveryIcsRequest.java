package com.personal.marketnote.fulfillment.adapter.in.web.vendor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class RegisterFulfillmentDeliveryIcsRequest {
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
            name = "platform",
            description = "쇼핑몰명(QOO10, SHOPEE …)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "쇼핑몰명은 필수값입니다.")
    private String platform;

    @Schema(
            name = "logiCenter",
            description = "배송센터(DRA, ICS …)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "배송센터는 필수값입니다.")
    private String logiCenter;

    @Schema(
            name = "invoiceNo",
            description = "운송장번호",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "운송장번호는 필수값입니다.")
    private String invoiceNo;

    @Schema(
            name = "custNm",
            description = "배송 고객명",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String custNm;

    @Schema(
            name = "custTelNo",
            description = "배송 고객번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String custTelNo;

    @Schema(
            name = "custAddr",
            description = "배송주소",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String custAddr;

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
            description = "판매채널(큐텐, 쇼피…)",
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
            name = "remark",
            description = "비고(파스토에 요청하거나 공유할 내용)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String remark;

    @Schema(
            name = "godCds",
            description = "출고 상품 코드 목록",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Valid
    @NotEmpty(message = "출고 상품 목록은 필수값입니다.")
    private List<RegisterFulfillmentDeliveryIcsGoodsRequest> godCds;
}
