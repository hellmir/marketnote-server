package com.personal.marketnote.notification.domain.aligo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrandTalkCommerceInfoSnapshotState {
    private final String title;
    private final String regularPrice;
    private final String discountPrice;
    private final String discountRate;
    private final String discountFixed;
}
