package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class BrandTalkButton {

    private String name;
    private AligoTemplateButtonLinkType linkType;
    private String linkM;
    private String linkP;
    private String linkI;
    private String linkA;

    public static BrandTalkButton of(
            String name, AligoTemplateButtonLinkType linkType,
            String linkM, String linkP, String linkI, String linkA
    ) {
        validate(name, linkType, linkM, linkP, linkI, linkA);
        return BrandTalkButton.builder()
                .name(name)
                .linkType(linkType)
                .linkM(linkM)
                .linkP(linkP)
                .linkI(linkI)
                .linkA(linkA)
                .build();
    }

    private static void validate(
            String name, AligoTemplateButtonLinkType linkType,
            String linkM, String linkP, String linkI, String linkA
    ) {
        if (FormatValidator.hasNoValue(name)) {
            throw new InvalidBrandTalkButtonException("버튼 name은 필수입니다.");
        }
        if (linkType.requiresWebLink()) {
            if (FormatValidator.hasNoValue(linkM) || FormatValidator.hasNoValue(linkP)) {
                throw new InvalidBrandTalkButtonException("WL 타입 버튼은 linkM, linkP가 필수입니다.");
            }
        }
        if (linkType.requiresAppLink()) {
            if (FormatValidator.hasNoValue(linkI) || FormatValidator.hasNoValue(linkA)) {
                throw new InvalidBrandTalkButtonException("AL 타입 버튼은 linkI, linkA가 필수입니다.");
            }
        }
    }
}
