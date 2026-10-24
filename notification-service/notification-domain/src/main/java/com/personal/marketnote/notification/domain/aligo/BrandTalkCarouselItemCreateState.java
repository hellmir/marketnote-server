package com.personal.marketnote.notification.domain.aligo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrandTalkCarouselItemCreateState {
    private final String header;
    private final String content;
    private final String imageUrl;
    private final List<BrandTalkButton> buttons;
    private final BrandTalkCoupon coupon;
}
