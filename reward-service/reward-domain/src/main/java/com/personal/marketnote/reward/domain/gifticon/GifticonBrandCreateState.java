package com.personal.marketnote.reward.domain.gifticon;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GifticonBrandCreateState {
    private final String brandCode;
    private final String brandName;
    private final String brandImageUrl;
}
