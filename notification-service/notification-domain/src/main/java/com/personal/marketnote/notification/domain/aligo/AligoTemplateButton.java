package com.personal.marketnote.notification.domain.aligo;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class AligoTemplateButton {

    private int ordering;
    private String name;
    private AligoTemplateButtonLinkType linkType;
    private String linkMo;
    private String linkPc;
    private String linkIos;
    private String linkAnd;

    public static AligoTemplateButton from(AligoTemplateButtonCreateState state) {
        return AligoTemplateButton.builder()
                .ordering(state.getOrdering())
                .name(state.getName())
                .linkType(state.getLinkType())
                .linkMo(state.getLinkMo())
                .linkPc(state.getLinkPc())
                .linkIos(state.getLinkIos())
                .linkAnd(state.getLinkAnd())
                .build();
    }
}
