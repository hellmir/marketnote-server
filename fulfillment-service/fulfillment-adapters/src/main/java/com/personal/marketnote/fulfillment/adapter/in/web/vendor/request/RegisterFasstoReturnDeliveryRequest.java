package com.personal.marketnote.fulfillment.adapter.in.web.vendor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class RegisterFasstoReturnDeliveryRequest {
    @Schema(
            name = "ordDt",
            description = "반품예정일(필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "반품예정일은 필수값입니다.")
    private String ordDt;

    @Schema(
            name = "ordNo",
            description = "주문번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String ordNo;

    @Schema(
            name = "parcelCd",
            description = "원택배사코드(필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "원택배사코드는 필수값입니다.")
    private String parcelCd;

    @Schema(
            name = "invoiceNo",
            description = "원송장번호(필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "원송장번호는 필수값입니다.")
    private String invoiceNo;

    @Schema(
            name = "custNm",
            description = "고객명(필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "고객명은 필수값입니다.")
    private String custNm;

    @Schema(
            name = "custTelNo",
            description = "고객 전화번호(필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "고객 전화번호는 필수값입니다.")
    private String custTelNo;

    @Schema(
            name = "custAddr",
            description = "고객 주소(필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "고객 주소는 필수값입니다.")
    private String custAddr;

    @Schema(
            name = "rtnEmpNm",
            description = "담당자명",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String rtnEmpNm;

    @Schema(
            name = "rtnTelNo",
            description = "반품 전화번호(공백시 센터로)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String rtnTelNo;

    @Schema(
            name = "rtnZipCd",
            description = "반품 우편번호(공백시 센터로)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String rtnZipCd;

    @Schema(
            name = "rtnAddr1",
            description = "반품 주소1(공백시 센터로)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String rtnAddr1;

    @Schema(
            name = "rtnAddr2",
            description = "반품 주소2(공백시 센터로)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String rtnAddr2;

    @Schema(
            name = "rtnGubun",
            description = "반품 구분코드(01:반품, 02:교환, 03:환불)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String rtnGubun;

    @Schema(
            name = "rtnReason",
            description = "반품 사유 코드",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String rtnReason;

    @Schema(
            name = "getRtnDetailReason",
            description = "반품 사유 상세",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String getRtnDetailReason;

    @Schema(
            name = "rtnShipReqTerm",
            description = "반품 배송요청사항",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String rtnShipReqTerm;

    @Schema(
            name = "godCds",
            description = "반품 상품 코드 목록",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Valid
    private List<RegisterFasstoDeliveryGoodsRequest> godCds;
}
