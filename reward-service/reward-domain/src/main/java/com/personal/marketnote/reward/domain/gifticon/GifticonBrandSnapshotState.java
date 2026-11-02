package com.personal.marketnote.reward.domain.gifticon;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GifticonBrandSnapshotState {
    private final Long id;
    private final String brandCode;
    private final String brandName;
    private final String brandImageUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
