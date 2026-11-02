package com.personal.marketnote.reward.domain.exception;

public class GifticonGoodsNotExposedException extends IllegalStateException {
    private static final String MESSAGE = "노출되지 않은 상품에는 정렬 순서를 설정할 수 없습니다. goodsCode=%s";

    public GifticonGoodsNotExposedException(String goodsCode) {
        super(String.format(MESSAGE, goodsCode));
    }
}
