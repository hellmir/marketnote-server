package com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception;

public class InvalidGiftishowBrandCodeException extends IllegalArgumentException {

    public InvalidGiftishowBrandCodeException() {
        super("기프티쇼 브랜드코드(brandCode)는 필수입니다.");
    }
}
