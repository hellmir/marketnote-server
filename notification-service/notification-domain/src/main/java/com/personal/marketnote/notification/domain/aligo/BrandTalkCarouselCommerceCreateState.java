package com.personal.marketnote.notification.domain.aligo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrandTalkCarouselCommerceCreateState {
    private final String title;
    private final String regularPrice;
    private final String discountPrice;
    private final String discountRate;
    private final String discountFixed;
}
