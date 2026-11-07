package com.personal.marketnote.reward.domain.exception;

public class GifticonGoodsNotSaleException extends IllegalStateException {
    private static final String MESSAGE = "ERR_GIFTICON_GOODS_SALE_01::SALE 상태가 아닌 상품은 노출할 수 없습니다.";

    public GifticonGoodsNotSaleException() {
        super(MESSAGE);
    }
}
