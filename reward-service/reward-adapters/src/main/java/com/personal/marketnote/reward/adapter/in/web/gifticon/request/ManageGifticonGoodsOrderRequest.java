package com.personal.marketnote.reward.adapter.in.web.gifticon.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ManageGifticonGoodsOrderRequest(
        @NotEmpty(message = "순서 관리 항목은 필수입니다")
        @Valid
        List<OrderItem> items
) {
    public record OrderItem(
            @NotNull(message = "상품 코드는 필수입니다")
            String goodsCode,

            Integer orderNum
    ) {
    }
}
