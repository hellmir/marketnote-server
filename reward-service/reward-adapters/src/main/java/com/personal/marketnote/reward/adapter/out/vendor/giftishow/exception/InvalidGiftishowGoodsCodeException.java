package com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception;

public class InvalidGiftishowGoodsCodeException extends IllegalArgumentException {

    public InvalidGiftishowGoodsCodeException() {
        super("기프티쇼 상품코드(goodsCode)는 필수입니다.");
    }
}
