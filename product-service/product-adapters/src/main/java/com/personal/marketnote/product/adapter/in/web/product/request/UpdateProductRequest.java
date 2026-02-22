package com.personal.marketnote.product.adapter.in.web.product.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateProductRequest(
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
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "브랜드명은 필수값입니다.")
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
                name = "fulfillmentVendorGoods",
                description = "파스토 상품 수정 파라미터",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Valid
        UpdateProductFulfillmentVendorGoodsRequest fulfillmentVendorGoods
) {
    public UpdateProductRequest {
        isFindAllOptions = isFindAllOptions != null ? isFindAllOptions : true;
    }
}
