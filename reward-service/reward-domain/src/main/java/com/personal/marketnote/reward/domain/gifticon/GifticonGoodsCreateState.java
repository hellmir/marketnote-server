package com.personal.marketnote.reward.domain.gifticon;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GifticonGoodsCreateState {
    private final String goodsCode;
    private final String goodsName;
    private final String brandCode;
    private final String brandName;
    private final String brandImageUrl;
    private final String categoryCode;
    private final Long realPrice;
    private final Long salePrice;
    private final Long cashPrice;
    private final String imageUrl;
    private final String description;
    private final Integer validDays;
    private final String goodsStatus;
}
