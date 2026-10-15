package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class BrandTalkCarouselTail {

    private String linkM;

    public static BrandTalkCarouselTail of(String linkM) {
        validate(linkM);
        return BrandTalkCarouselTail.builder()
                .linkM(linkM)
                .build();
    }

    private static void validate(String linkM) {
        if (FormatValidator.hasNoValue(linkM)) {
            throw new InvalidBrandTalkTemplateException("캐러셀 tail의 linkM은 필수입니다.");
        }
    }
}
