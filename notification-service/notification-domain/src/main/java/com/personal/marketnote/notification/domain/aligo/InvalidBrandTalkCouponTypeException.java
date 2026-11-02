package com.personal.marketnote.notification.domain.aligo;

public class InvalidBrandTalkCouponTypeException extends IllegalArgumentException {

    public InvalidBrandTalkCouponTypeException(String code) {
        super("ERR_BRAND_TALK_02::알 수 없는 브랜드메시지 쿠폰 타입: " + code);
    }
}
