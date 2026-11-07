package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCoupon;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCouponCreateState;
import com.personal.marketnote.notification.port.in.command.BrandTalkCouponCommand;

public class BrandTalkCouponCommandToStateMapper {

    private BrandTalkCouponCommandToStateMapper() {
    }

    public static BrandTalkCoupon mapCoupon(BrandTalkCouponCommand couponCommand) {
        if (FormatValidator.hasNoValue(couponCommand)) {
            return null;
        }
        return BrandTalkCoupon.from(
                BrandTalkCouponCreateState.builder()
                        .title(couponCommand.title())
                        .description(couponCommand.description())
                        .linkM(couponCommand.linkM())
                        .linkP(couponCommand.linkP())
                        .linkI(couponCommand.linkI())
                        .linkA(couponCommand.linkA())
                        .build()
        );
    }
}
