package com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception;

public class InvalidGiftishowTrIdException extends IllegalArgumentException {

    public InvalidGiftishowTrIdException() {
        super("기프티쇼 거래ID(trId)는 필수입니다.");
    }
}
