package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class BrandTalkWideItem {

    private String title;
    private String imageUrl;
    private String linkM;
    private String linkP;
    private String linkI;
    private String linkA;

    public static BrandTalkWideItem of(String title, String imageUrl, String linkM, String linkP, String linkI, String linkA) {
        validate(title);
        return BrandTalkWideItem.builder()
                .title(title)
                .imageUrl(imageUrl)
                .linkM(linkM)
                .linkP(linkP)
                .linkI(linkI)
                .linkA(linkA)
                .build();
    }

    private static void validate(String title) {
        if (FormatValidator.hasNoValue(title)) {
            throw new InvalidBrandTalkTemplateException("와이드 아이템의 title은 필수입니다.");
        }
    }
}
