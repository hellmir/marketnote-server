package com.personal.marketnote.reward.port.in.result.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;

import java.util.List;

public record GetAdminGifticonCategoriesResult(List<GifticonCategoryItemResult> categories) {

    public static GetAdminGifticonCategoriesResult from(List<GifticonCategory> categories) {
        List<GifticonCategoryItemResult> items = categories.stream()
                .map(category -> new GifticonCategoryItemResult(
                        category.getId(),
                        category.getCategoryCode(),
                        category.getCategoryName(),
                        category.getDisplayName(),
                        category.getEffectiveDisplayName(),
                        category.getIconUrl(),
                        category.isExposed(),
                        category.getOrderNum(),
                        category.getCreatedAt(),
                        category.getModifiedAt()
                ))
                .toList();
        return new GetAdminGifticonCategoriesResult(items);
    }
}
