package com.personal.marketnote.notification.domain.aligo;

public class InvalidBrandTalkCouponException extends IllegalArgumentException {

    public InvalidBrandTalkCouponException(String message) {
        super("ERR_BRAND_TALK_04::" + message);
    }
}
