package com.personal.marketnote.reward.adapter.in.web.gifticon.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ManageGifticonGoodsExposureRequest(
        @NotEmpty(message = "노출 관리 항목은 필수입니다")
        @Valid
        List<ExposureItem> items
) {
    public record ExposureItem(
            @NotNull(message = "상품 코드는 필수입니다")
            String goodsCode,

            @NotNull(message = "노출 여부는 필수입니다")
            Boolean exposed
    ) {
    }
}
