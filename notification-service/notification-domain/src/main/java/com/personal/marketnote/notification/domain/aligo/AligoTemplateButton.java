package com.personal.marketnote.notification.domain.aligo;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class AligoTemplateButton {

    private int ordering;
    private String name;
    private AligoTemplateButtonLinkType linkType;
    private String linkMo;
    private String linkPc;
    private String linkIos;
    private String linkAnd;
}
