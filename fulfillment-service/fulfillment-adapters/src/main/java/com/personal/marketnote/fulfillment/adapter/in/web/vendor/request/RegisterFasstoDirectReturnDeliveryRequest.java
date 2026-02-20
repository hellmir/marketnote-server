package com.personal.marketnote.fulfillment.adapter.in.web.vendor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class RegisterFasstoDirectReturnDeliveryRequest {
    @Schema(
            name = "ordDt",
            description = "반품예정일(필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "반품예정일은 필수값입니다.")
    private String ordDt;

    @Schema(
            name = "supCd",
            description = "공급처코드",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String supCd;

    @Schema(
            name = "orgParcelCd",
            description = "원택배사코드(필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "원택배사코드는 필수값입니다.")
    private String orgParcelCd;

    @Schema(
            name = "orgInvoiceNo",
            description = "원송장번호(필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "원송장번호는 필수값입니다.")
    private String orgInvoiceNo;

    @Schema(
            name = "inWay",
            description = "반품방식(필수, 01:택배, 02:차량)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "반품방식은 필수값입니다.")
    private String inWay;

    @Schema(
            name = "custNm",
            description = "고객명(필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "고객명은 필수값입니다.")
    private String custNm;

    @Schema(
            name = "rtnParcelComp",
            description = "반품택배사명(택배사코드 사용하지 않음)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String rtnParcelComp;

    @Schema(
            name = "rtnInvoiceNo",
            description = "반품송장번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String rtnInvoiceNo;

    @Schema(
            name = "rtnGubun",
            description = "반품 구분코드(필수, 01:반품, 02:교환, 03:환불)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "반품 구분코드는 필수값입니다.")
    private String rtnGubun;

    @Schema(
            name = "rtnReason",
            description = "반품 사유 코드(필수, 01:구매 의사 취소, 02:색상 및 사이즈 변경, 03:다른 상품 잘못 주문, 04:서비스 불만족, 05:배송 지연, 06:배송 누락, 07:상품 파손, 08:상품 정보 상이, 09:배송 주소 상이, 10:색상 등 다른상품 잘못 배송, 99:기타)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "반품 사유 코드는 필수값입니다.")
    private String rtnReason;

    @Schema(
            name = "rtnDetailReason",
            description = "반품 사유 상세내용",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String rtnDetailReason;

    @Schema(
            name = "remark",
            description = "비고",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String remark;

    @Schema(
            name = "godCds",
            description = "반품 대상 상품 목록",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Valid
    private List<RegisterFasstoDeliveryGoodsRequest> godCds;
}
