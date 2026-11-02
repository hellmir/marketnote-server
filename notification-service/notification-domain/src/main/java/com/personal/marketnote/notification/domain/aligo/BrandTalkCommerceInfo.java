package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class BrandTalkCommerceInfo {

    private String title;
    private String regularPrice;
    private String discountPrice;
    private String discountRate;
    private String discountFixed;

    public static BrandTalkCommerceInfo from(BrandTalkCommerceInfoCreateState state) {
        validate(state.getTitle(), state.getRegularPrice());
        return BrandTalkCommerceInfo.builder()
                .title(state.getTitle())
                .regularPrice(state.getRegularPrice())
                .discountPrice(state.getDiscountPrice())
                .discountRate(state.getDiscountRate())
                .discountFixed(state.getDiscountFixed())
                .build();
    }

    private static void validate(String title, String regularPrice) {
        if (FormatValidator.hasNoValue(title)) {
            throw new InvalidBrandTalkTemplateException("커머스의 title은 필수입니다.");
        }
        if (FormatValidator.hasNoValue(regularPrice)) {
            throw new InvalidBrandTalkTemplateException("커머스의 regularPrice는 필수입니다.");
        }
    }
}
