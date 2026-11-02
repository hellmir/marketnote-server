package com.personal.marketnote.reward.domain.gifticon;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GifticonCategorySnapshotState {
    private final Long id;
    private final String categoryCode;
    private final String categoryName;
    private final String displayName;
    private final String iconUrl;
    private final boolean exposed;
    private final Integer orderNum;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
