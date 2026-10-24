package com.personal.marketnote.reward.exception;

public class InvalidGifticonOrderPageSizeException extends IllegalArgumentException {
    public InvalidGifticonOrderPageSizeException(int pageSize) {
        super("ERR_GIFTICON_ORDER_01::기프티콘 주문 조회 pageSize는 1 이상 100 이하여야 합니다. pageSize=" + pageSize);
    }
}
