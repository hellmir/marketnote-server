package com.personal.marketnote.reward.domain.gifticon;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GifticonCategoryCreateState {
    private final String categoryCode;
    private final String categoryName;
}
