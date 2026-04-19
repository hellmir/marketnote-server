package com.personal.marketnote.fulfillment.adapter.in.web.vendor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class UpdateFulfillmentSupplierRequest {
    @Schema(
            name = "customerSupplierCode",
            description = "고객사 공급사 코드",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String customerSupplierCode;

    @Schema(
            name = "supplierCode",
            description = "공급사 코드(수정 시 필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "공급사 코드는 필수값입니다.")
    private String supplierCode;

    @Schema(
            name = "supplierName",
            description = "공급사명",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "공급사명은 필수값입니다.")
    private String supplierName;

    @Schema(
            name = "useYn",
            description = "사용여부",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String useYn;

    @Schema(
            name = "dealStartDate",
            description = "거래시작일자",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String dealStartDate;

    @Schema(
            name = "dealEndDate",
            description = "거래종료일자",
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
            name = "businessCategory",
            description = "업태",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String businessCategory;

    @Schema(
            name = "businessType",
            description = "업종",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String businessType;

    @Schema(
            name = "phoneNumber",
            description = "전화번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String phoneNumber;

    @Schema(
            name = "faxNumber",
            description = "팩스번호",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String faxNumber;

    @Schema(
            name = "primaryManagerName",
            description = "담당자명1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String primaryManagerName;

    @Schema(
            name = "primaryManagerPosition",
            description = "담당자직위1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String primaryManagerPosition;

    @Schema(
            name = "primaryManagerPhoneNumber",
            description = "담당자전화번호1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String primaryManagerPhoneNumber;

    @Schema(
            name = "primaryManagerEmail",
            description = "담당자이메일1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String primaryManagerEmail;

    @Schema(
            name = "secondaryManagerName",
            description = "담당자명2",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String secondaryManagerName;

    @Schema(
            name = "secondaryManagerPosition",
            description = "담당자직위2",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String secondaryManagerPosition;

    @Schema(
            name = "secondaryManagerPhoneNumber",
            description = "담당자전화번호2",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String secondaryManagerPhoneNumber;

    @Schema(
            name = "secondaryManagerEmail",
            description = "담당자이메일2",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String secondaryManagerEmail;
}
