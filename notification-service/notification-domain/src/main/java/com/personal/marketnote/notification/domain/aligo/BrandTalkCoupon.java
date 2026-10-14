package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class BrandTalkCoupon {

    private static final int MAX_DESCRIPTION_LENGTH = 128;

    private String title;
    private String description;
    private String linkM;
    private String linkP;
    private String linkI;
    private String linkA;

    public static BrandTalkCoupon of(
            String title, String description,
            String linkM, String linkP, String linkI, String linkA
    ) {
        validate(title, description);
        return BrandTalkCoupon.builder()
                .title(title)
                .description(description)
                .linkM(linkM)
                .linkP(linkP)
                .linkI(linkI)
                .linkA(linkA)
                .build();
    }

    private static void validate(String title, String description) {
        if (FormatValidator.hasNoValue(title)) {
            throw new InvalidBrandTalkCouponException("쿠폰 title은 필수입니다.");
        }
        if (FormatValidator.hasValue(description) && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidBrandTalkCouponException(
                    "쿠폰 description은 최대 " + MAX_DESCRIPTION_LENGTH + "자까지 입력할 수 있습니다."
            );
        }
    }
}
