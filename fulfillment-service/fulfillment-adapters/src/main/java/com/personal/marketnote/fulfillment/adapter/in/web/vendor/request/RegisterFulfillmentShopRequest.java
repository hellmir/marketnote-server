package com.personal.marketnote.fulfillment.adapter.in.web.vendor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class RegisterFulfillmentShopRequest {
    @Schema(
            name = "shopName",
            description = "출고처명",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "출고처명은 필수값입니다.")
    private String shopName;

    @Schema(
            name = "customerShopCode",
            description = "고객사 출고처 코드(없으면 자동 생성)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String customerShopCode;

    @Schema(
            name = "dealStartDate",
            description = "거래 시작일자",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String dealStartDate;

    @Schema(
            name = "dealEndDate",
            description = "거래 종료일자",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String dealEndDate;

    @Schema(
            name = "zipCode",
            description = "우편번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String zipCode;

    @Schema(
            name = "address1",
            description = "주소1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String address1;

    @Schema(
            name = "address2",
            description = "주소2",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String address2;

    @Schema(
            name = "ceoName",
            description = "대표자명",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String ceoName;

    @Schema(
            name = "businessNumber",
            description = "사업자번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String businessNumber;

    @Schema(
            name = "phoneNumber",
            description = "전화번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String phoneNumber;

    @Schema(
            name = "unloadMethod",
            description = "하차방식(01: 지게차, 02: 수작업)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String unloadMethod;

    @Schema(
            name = "inspectionMethod",
            description = "검수방식(01: 전수검수, 02: 샘플검수)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String inspectionMethod;

    @Schema(
            name = "standbyYn",
            description = "대기여부",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String standbyYn;

    @Schema(
            name = "formType",
            description = "거래명세서 양식(STDF001: 택배기본, STDF002: 차량기본)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String formType;

    @Schema(
            name = "managerName",
            description = "담당자명",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String managerName;

    @Schema(
            name = "managerPosition",
            description = "담당자 직위",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String managerPosition;

    @Schema(
            name = "managerPhoneNumber",
            description = "담당자 전화번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String managerPhoneNumber;

    @Schema(
            name = "useYn",
            description = "사용 여부",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String useYn;
}
