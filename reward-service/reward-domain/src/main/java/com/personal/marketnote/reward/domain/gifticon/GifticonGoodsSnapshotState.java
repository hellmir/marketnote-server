package com.personal.marketnote.reward.domain.gifticon;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GifticonGoodsSnapshotState {
    private final Long id;
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
    private final boolean exposed;
    private final boolean popular;
    private final Integer orderNum;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
