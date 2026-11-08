package com.personal.marketnote.notification.domain.aligo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AligoTemplateButtonSnapshotState {
    private final int ordering;
    private final String name;
    private final AligoTemplateButtonLinkType linkType;
    private final String linkMo;
    private final String linkPc;
    private final String linkIos;
    private final String linkAnd;
}
