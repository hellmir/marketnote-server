package com.personal.marketnote.reward.adapter.in.web.gifticon.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ManageGifticonGoodsCashPriceRequest(
        @NotEmpty(message = "현금가 관리 항목은 필수입니다")
        @Valid
        List<CashPriceItem> items
) {
    public record CashPriceItem(
            @NotNull(message = "상품 코드는 필수입니다")
            String goodsCode,

            @NotNull(message = "현금가는 필수입니다")
            @Min(value = 0, message = "현금가는 0 이상이어야 합니다")
            Long cashPrice
    ) {
    }
}
