package com.personal.marketnote.reward.adapter.in.web.gifticon.response;

import com.personal.marketnote.reward.port.in.result.gifticon.GifticonCategoryItemResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GetAdminGifticonCategoriesResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class GetAdminGifticonCategoriesResponse {
    private final List<GifticonCategoryItem> categories;

    public static GetAdminGifticonCategoriesResponse from(GetAdminGifticonCategoriesResult result) {
        return GetAdminGifticonCategoriesResponse.builder()
                .categories(result.categories().stream()
                        .map(GifticonCategoryItem::from)
                        .toList())
                .build();
    }

    @Getter
    @Builder
    public static class GifticonCategoryItem {
        private final Long id;
        private final String categoryCode;
        private final String categoryName;
        private final String displayName;
        private final String effectiveDisplayName;
        private final String iconUrl;
        private final boolean exposed;
        private final Integer orderNum;
        private final LocalDateTime createdAt;
        private final LocalDateTime modifiedAt;

        public static GifticonCategoryItem from(GifticonCategoryItemResult item) {
            return GifticonCategoryItem.builder()
                    .id(item.id())
                    .categoryCode(item.categoryCode())
                    .categoryName(item.categoryName())
                    .displayName(item.displayName())
                    .effectiveDisplayName(item.effectiveDisplayName())
                    .iconUrl(item.iconUrl())
                    .exposed(item.exposed())
                    .orderNum(item.orderNum())
                    .createdAt(item.createdAt())
                    .modifiedAt(item.modifiedAt())
                    .build();
        }
    }
}
