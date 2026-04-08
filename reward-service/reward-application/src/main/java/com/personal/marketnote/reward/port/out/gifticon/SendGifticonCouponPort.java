package com.personal.marketnote.reward.port.out.gifticon;

import lombok.Builder;

public interface SendGifticonCouponPort {
    SendCouponResult sendCoupon(String trId, String goodsCode, String userId);

    @Builder
    record SendCouponResult(
            boolean success,
            String orderNo,
            String pinNo,
            String couponImageUrl,
            String validEndDate,
            String errorCode,
            String errorMessage
    ) {
    }
}
