package com.personal.marketnote.reward.adapter.in.web.gifticon.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ManageFeaturedGifticonGoodsRequest(
        @NotEmpty(message = "인기상품 관리 항목은 필수입니다")
        @Size(max = 10, message = "인기상품은 최대 10개까지 설정 가능합니다")
        @Valid
        List<FeaturedGoodsItem> items
) {
    public record FeaturedGoodsItem(
            @NotNull(message = "상품 코드는 필수입니다")
            String goodsCode,

            @NotNull(message = "인기상품 여부는 필수입니다")
            Boolean popular,

            Integer popularOrderNum
    ) {
    }
}
