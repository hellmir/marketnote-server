package com.personal.marketnote.reward.port.in.result.gifticon;

import java.time.LocalDateTime;

public record GifticonCategoryItemResult(
        Long id,
        String categoryCode,
        String categoryName,
        String displayName,
        String effectiveDisplayName,
        String iconUrl,
        boolean exposed,
        Integer orderNum,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}
