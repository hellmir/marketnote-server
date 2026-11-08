package com.personal.marketnote.notification.domain.aligo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BrandTalkCarouselItemSnapshotState {
    private final String header;
    private final String content;
    private final String imageUrl;
    private final List<BrandTalkButtonSnapshotState> buttons;
    private final BrandTalkCouponSnapshotState coupon;
}
