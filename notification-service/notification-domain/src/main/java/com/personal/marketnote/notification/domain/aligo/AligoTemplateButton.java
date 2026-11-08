package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
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
        validate(
                state.getName(), state.getLinkType(),
                state.getLinkMo(), state.getLinkPc(), state.getLinkIos(), state.getLinkAnd()
        );
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

    public static AligoTemplateButton from(AligoTemplateButtonSnapshotState state) {
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

    private static void validate(
            String name, AligoTemplateButtonLinkType linkType,
            String linkMo, String linkPc, String linkIos, String linkAnd
    ) {
        if (FormatValidator.hasNoValue(name)) {
            throw new InvalidAligoTemplateButtonException("버튼 name은 필수입니다.");
        }
        if (linkType.requiresWebLink()) {
            if (FormatValidator.hasNoValue(linkMo) || FormatValidator.hasNoValue(linkPc)) {
                throw new InvalidAligoTemplateButtonException("WL 타입 버튼은 linkMo, linkPc가 필수입니다.");
            }
        }
        if (linkType.requiresAppLink()) {
            if (FormatValidator.hasNoValue(linkIos) || FormatValidator.hasNoValue(linkAnd)) {
                throw new InvalidAligoTemplateButtonException("AL 타입 버튼은 linkIos, linkAnd가 필수입니다.");
            }
        }
    }
}
