package com.personal.marketnote.notification.domain.aligo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrandTalkButtonSnapshotState {
    private final String name;
    private final AligoTemplateButtonLinkType linkType;
    private final String linkM;
    private final String linkP;
    private final String linkI;
    private final String linkA;
}
