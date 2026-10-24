package com.personal.marketnote.notification.domain.aligo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrandTalkButtonCreateState {
    private final String name;
    private final AligoTemplateButtonLinkType linkType;
    private final String linkM;
    private final String linkP;
    private final String linkI;
    private final String linkA;
}
