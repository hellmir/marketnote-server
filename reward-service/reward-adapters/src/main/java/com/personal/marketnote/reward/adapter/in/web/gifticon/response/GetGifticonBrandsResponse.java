package com.personal.marketnote.reward.adapter.in.web.gifticon.response;

import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonBrandsResult;

import java.util.List;

public record GetGifticonBrandsResponse(List<GifticonBrandItemResponse> brands) {

    public static GetGifticonBrandsResponse from(GetGifticonBrandsResult result) {
        List<GifticonBrandItemResponse> items = result.brands().stream()
                .map(item -> new GifticonBrandItemResponse(
                        item.brandCode(),
                        item.brandName(),
                        item.brandImageUrl()
                ))
                .toList();

        return new GetGifticonBrandsResponse(items);
    }

    public record GifticonBrandItemResponse(
            String brandCode,
            String brandName,
            String brandImageUrl
    ) {
    }
}
