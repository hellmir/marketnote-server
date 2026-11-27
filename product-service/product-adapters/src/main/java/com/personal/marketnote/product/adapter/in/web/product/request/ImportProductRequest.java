package com.personal.marketnote.product.adapter.in.web.product.request;

import com.personal.marketnote.common.utility.FormatValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.List;

public record ImportProductRequest(
    @Schema(
        name = "sellerId",
        description = "판매자 회원 ID",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "판매자 회원 ID는 필수값입니다.")
    @Min(value = 1, message = "판매자 회원 ID는 1 이상이어야 합니다.")
    Long sellerId,

    @Schema(
        name = "name",
        description = "상품명",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "상품명은 필수값입니다.")
    @Size(min = 1, max = 255, message = "상품명은 1~255자입니다.")
    String name,

    @Schema(
        name = "brandName",
        description = "브랜드명",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 255, message = "브랜드명은 최대 255자입니다.")
    String brandName,

    @Schema(
        name = "detail",
        description = "상품 설명",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "상품 설명은 필수값입니다.")
    @Size(max = 1023, message = "상품 설명은 최대 1023자입니다.")
    String detail,

    @Schema(
        name = "price",
        description = "상품 기본 판매 가격(원)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "상품 기본 판매 가격은 필수값입니다.")
    @Min(value = 0, message = "상품 기본 판매 가격은 0 이상이어야 합니다.")
    Long price,

    @Schema(
        name = "discountPrice",
        description = "상품 할인 판매 가격(원)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "상품 할인 판매 가격은 필수값입니다.")
    @Min(value = 0, message = "상품 할인 판매 가격은 0 이상이어야 합니다.")
    Long discountPrice,

    @Schema(
        name = "accumulatedPoint",
        description = "상품 적립 포인트(원)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "상품 적립 포인트는 필수값입니다.")
    @Min(value = 0, message = "상품 적립 포인트는 0 이상이어야 합니다.")
    Long accumulatedPoint,

    @Schema(
        name = "isFindAllOptions",
        description = "상품 목록 조회 시 옵션마다 개별 상품으로 조회 여부",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    Boolean isFindAllOptions,

    @Schema(
        name = "tags",
        description = "상품 태그 목록",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "상품 태그 목록은 필수값입니다.")
    List<String> tags,

    @Schema(
        name = "fasstoProductCode",
        description = "파스토에 이미 등록된 상품의 운영코드(cstGodCd). 입력 시 Fastto 송신 스킵.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "fasstoProductCode는 필수값입니다.")
    @Size(max = 25, message = "fasstoProductCode는 최대 25자입니다.")
    @Pattern(
        regexp = "^[0-9A-Za-z]{1,25}$",
        message = "fasstoProductCode는 1-25자 영숫자만 허용됩니다."
    )
    String fasstoProductCode
) {
    public ImportProductRequest {
        isFindAllOptions = FormatValidator.hasValue(isFindAllOptions) ? isFindAllOptions : true;
    }
}
