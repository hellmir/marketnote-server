package com.personal.marketnote.fulfillment.adapter.in.web.vendor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class UpdateFulfillmentShopRequest {
    @Schema(
            name = "shopCd",
            description = "출고처 코드(수정 시 필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "출고처 코드는 필수값입니다.")
    private String shopCd;

    @Schema(
            name = "shopNm",
            description = "출고처명",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "출고처명은 필수값입니다.")
    private String shopNm;

    @Schema(
            name = "cstShopCd",
            description = "고객사 출고처 코드(없으면 자동 생성)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String cstShopCd;

    @Schema(
            name = "dealStrDt",
            description = "거래 시작일자",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String dealStrDt;

    @Schema(
            name = "dealEndDt",
            description = "거래 종료일자",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String dealEndDt;

    @Schema(
            name = "zipNo",
            description = "우편번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String zipNo;

    @Schema(
            name = "addr1",
            description = "주소1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String addr1;

    @Schema(
            name = "addr2",
            description = "주소2",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String addr2;

    @Schema(
            name = "ceoNm",
            description = "대표자명",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String ceoNm;

    @Schema(
            name = "busNo",
            description = "사업자번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String busNo;

    @Schema(
            name = "telNo",
            description = "전화번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String telNo;

    @Schema(
            name = "unloadWay",
            description = "하차방식(01: 지게차, 02: 수작업)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String unloadWay;

    @Schema(
            name = "checkWay",
            description = "검수방식(01: 전수검수, 02: 샘플검수)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String checkWay;

    @Schema(
            name = "standYn",
            description = "대기여부",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String standYn;

    @Schema(
            name = "formType",
            description = "거래명세서 양식(STDF001: 택배기본, STDF002: 차량기본)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String formType;

    @Schema(
            name = "empNm",
            description = "담당자명",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String empNm;

    @Schema(
            name = "empPosit",
            description = "담당자 직위",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String empPosit;

    @Schema(
            name = "empTelNo",
            description = "담당자 전화번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String empTelNo;

    @Schema(
            name = "useYn",
            description = "사용 여부",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String useYn;
}
