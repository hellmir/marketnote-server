package com.personal.marketnote.reward.port.out.gifticon;

import lombok.Builder;

public interface QueryGifticonCouponStatusPort {
    CouponStatusResult queryStatus(String trId);

    @Builder
    record CouponStatusResult(
            boolean success,
            String pinStatusCd,
            String validPrdEndDt,
            String errorCode,
            String errorMessage
    ) {
    }
}
