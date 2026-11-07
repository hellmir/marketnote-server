package com.personal.marketnote.reward.domain.gifticon;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GifticonBrandCreateState {
    private final String brandCode;
    private final String brandName;
    private final String brandImageUrl;

    public static GifticonBrandCreateState of(String brandCode, String brandName, String brandImageUrl) {
        return GifticonBrandCreateState.builder()
                .brandCode(brandCode)
                .brandName(brandName)
                .brandImageUrl(brandImageUrl)
                .build();
    }
}
