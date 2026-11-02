package com.personal.marketnote.reward.adapter.in.web.gifticon.request;

import jakarta.validation.constraints.NotBlank;

public record PurchaseGifticonRequest(
        @NotBlank(message = "상품 코드는 필수입니다")
        String goodsCode
) {
}
