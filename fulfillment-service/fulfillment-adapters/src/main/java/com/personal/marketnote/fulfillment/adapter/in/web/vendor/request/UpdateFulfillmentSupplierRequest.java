package com.personal.marketnote.fulfillment.adapter.in.web.vendor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class UpdateFulfillmentSupplierRequest {
    @Schema(
            name = "cstSupCd",
            description = "고객사 공급사 코드",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String cstSupCd;

    @Schema(
            name = "supCd",
            description = "공급사 코드(수정 시 필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "공급사 코드는 필수값입니다.")
    private String supCd;

    @Schema(
            name = "supNm",
            description = "공급사명",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "공급사명은 필수값입니다.")
    private String supNm;

    @Schema(
            name = "useYn",
            description = "사용여부",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String useYn;

    @Schema(
            name = "dealStrDt",
            description = "거래시작일자",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String dealStrDt;

    @Schema(
            name = "dealEndDt",
            description = "거래종료일자",
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
            name = "busSp",
            description = "업태",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String busSp;

    @Schema(
            name = "busTp",
            description = "업종",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String busTp;

    @Schema(
            name = "telNo",
            description = "전화번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String telNo;

    @Schema(
            name = "faxNo",
            description = "팩스번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String faxNo;

    @Schema(
            name = "empNm1",
            description = "담당자명1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String empNm1;

    @Schema(
            name = "empPosit1",
            description = "담당자직위1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String empPosit1;

    @Schema(
            name = "empTelNo1",
            description = "담당자전화번호1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String empTelNo1;

    @Schema(
            name = "empEmail1",
            description = "담당자이메일1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String empEmail1;

    @Schema(
            name = "empNm2",
            description = "담당자명2",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String empNm2;

    @Schema(
            name = "empPosit2",
            description = "담당자직위2",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String empPosit2;

    @Schema(
            name = "empTelNo2",
            description = "담당자전화번호2",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String empTelNo2;

    @Schema(
            name = "empEmail2",
            description = "담당자이메일2",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String empEmail2;
}
