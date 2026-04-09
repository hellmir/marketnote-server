package com.personal.marketnote.reward.domain.exception;

public class GifticonGoodsNotSaleException extends IllegalStateException {
    private static final String MESSAGE = "SALE 상태가 아닌 상품은 노출할 수 없습니다. goodsCode=%s, goodsStatus=%s";

    public GifticonGoodsNotSaleException(String goodsCode, String goodsStatus) {
        super(String.format(MESSAGE, goodsCode, goodsStatus));
    }
}
