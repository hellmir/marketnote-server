package com.personal.marketnote.reward.domain.exception;

public class GifticonGoodsNotFoundException extends RuntimeException {
    private static final String MESSAGE = "기프티콘 상품을 찾을 수 없습니다. goodsCode=%s";

    public GifticonGoodsNotFoundException(String goodsCode) {
        super(String.format(MESSAGE, goodsCode));
    }
}
