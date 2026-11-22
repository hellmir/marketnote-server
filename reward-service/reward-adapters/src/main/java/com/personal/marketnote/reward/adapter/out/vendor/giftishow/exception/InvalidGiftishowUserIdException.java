package com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception;

public class InvalidGiftishowUserIdException extends IllegalArgumentException {

    public InvalidGiftishowUserIdException() {
        super("기프티쇼 사용자ID(userId)는 필수입니다.");
    }
}
