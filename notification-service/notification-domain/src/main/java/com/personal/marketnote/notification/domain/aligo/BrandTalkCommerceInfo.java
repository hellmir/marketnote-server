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

    public static BrandTalkCommerceInfo of(
            String title, String regularPrice,
            String discountPrice, String discountRate, String discountFixed
    ) {
        validate(title, regularPrice);
        return BrandTalkCommerceInfo.builder()
                .title(title)
                .regularPrice(regularPrice)
                .discountPrice(discountPrice)
                .discountRate(discountRate)
                .discountFixed(discountFixed)
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
