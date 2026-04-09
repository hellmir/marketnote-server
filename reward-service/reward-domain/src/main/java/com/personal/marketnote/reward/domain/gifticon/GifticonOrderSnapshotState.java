package com.personal.marketnote.reward.domain.gifticon;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class GifticonOrderSnapshotState {
    private final Long id;
    private final Long userId;
    private final String goodsCode;
    private final String goodsName;
    private final String brandName;
    private final String productImageUrl;
    private final String trId;
    private final String orderNo;
    private final Long cashPrice;
    private final GifticonOrderStatus orderStatus;
    private final String couponImageUrl;
    private final String pinNo;
    private final LocalDate validEndDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
