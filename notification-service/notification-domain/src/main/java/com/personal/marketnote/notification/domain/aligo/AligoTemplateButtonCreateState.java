package com.personal.marketnote.notification.domain.aligo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AligoTemplateButtonCreateState {
    private final int ordering;
    private final String name;
    private final AligoTemplateButtonLinkType linkType;
    private final String linkMo;
    private final String linkPc;
    private final String linkIos;
    private final String linkAnd;
}
