package com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception;

public class InvalidGiftishowPhoneNoException extends IllegalArgumentException {

    public InvalidGiftishowPhoneNoException() {
        super("기프티쇼 휴대폰번호(phoneNo)는 필수입니다.");
    }
}
