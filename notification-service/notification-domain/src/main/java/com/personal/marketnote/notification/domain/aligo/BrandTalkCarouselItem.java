package com.personal.marketnote.notification.domain.aligo;

import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class BrandTalkCarouselItem {

    private static final int MIN_BUTTONS = 1;
    private static final int MAX_BUTTONS = 2;

    private String header;
    private String content;
    private String imageUrl;
    private List<BrandTalkButton> buttons;
    private BrandTalkCoupon coupon;

    public static BrandTalkCarouselItem of(
            String header, String content, String imageUrl,
            List<BrandTalkButton> buttons, BrandTalkCoupon coupon
    ) {
        validate(buttons);
        return BrandTalkCarouselItem.builder()
                .header(header)
                .content(content)
                .imageUrl(imageUrl)
                .buttons(buttons != null ? buttons : List.of())
                .coupon(coupon)
                .build();
    }

    private static void validate(List<BrandTalkButton> buttons) {
        if (buttons == null || buttons.size() < MIN_BUTTONS) {
            throw new InvalidBrandTalkTemplateException(
                    "캐러셀 아이템의 버튼은 최소 " + MIN_BUTTONS + "개 필수입니다."
            );
        }
        if (buttons.size() > MAX_BUTTONS) {
            throw new InvalidBrandTalkTemplateException(
                    "캐러셀 아이템의 버튼은 최대 " + MAX_BUTTONS + "개까지 등록할 수 있습니다."
            );
        }
    }
}
