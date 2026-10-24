package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
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

    public static BrandTalkCarouselItem from(BrandTalkCarouselItemCreateState state) {
        List<BrandTalkButton> buttons = state.getButtons();
        validate(buttons);
        return BrandTalkCarouselItem.builder()
                .header(state.getHeader())
                .content(state.getContent())
                .imageUrl(state.getImageUrl())
                .buttons(FormatValidator.hasValue(buttons) ? buttons : List.of())
                .coupon(state.getCoupon())
                .build();
    }

    private static void validate(List<BrandTalkButton> buttons) {
        if (FormatValidator.hasNoValue(buttons) || buttons.size() < MIN_BUTTONS) {
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
