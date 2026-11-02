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

    public static BrandTalkCoupon from(BrandTalkCouponCreateState state) {
        validate(state.getTitle(), state.getDescription());
        return BrandTalkCoupon.builder()
                .title(state.getTitle())
                .description(state.getDescription())
                .linkM(state.getLinkM())
                .linkP(state.getLinkP())
                .linkI(state.getLinkI())
                .linkA(state.getLinkA())
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
