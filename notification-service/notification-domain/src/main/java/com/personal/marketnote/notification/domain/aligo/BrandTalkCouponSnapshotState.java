package com.personal.marketnote.notification.domain.aligo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrandTalkCouponSnapshotState {
    private final String title;
    private final String description;
    private final String linkM;
    private final String linkP;
    private final String linkI;
    private final String linkA;
}
