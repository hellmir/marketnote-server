package com.personal.marketnote.reward.domain.gifticon;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GifticonOrderCreateState {
    private final Long userId;
    private final String goodsCode;
    private final String goodsName;
    private final String brandName;
    private final String productImageUrl;
    private final String trId;
    private final Long cashPrice;
}
