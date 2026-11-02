package com.personal.marketnote.reward.adapter.in.web.gifticon.response;

import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonCategoriesResult;

import java.util.List;

public record GetGifticonCategoriesResponse(List<GifticonCategoryItemResponse> categories) {

    public static GetGifticonCategoriesResponse from(GetGifticonCategoriesResult result) {
        List<GifticonCategoryItemResponse> items = result.categories().stream()
                .map(item -> new GifticonCategoryItemResponse(
                        item.categoryCode(),
                        item.displayName(),
                        item.iconUrl(),
                        item.orderNum()
                ))
                .toList();

        return new GetGifticonCategoriesResponse(items);
    }

    public record GifticonCategoryItemResponse(
            String categoryCode,
            String displayName,
            String iconUrl,
            Integer orderNum
    ) {
    }
}
