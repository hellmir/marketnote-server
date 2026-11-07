package com.personal.marketnote.reward.domain.exception;

public class GifticonGoodsNotExposedException extends IllegalStateException {
    private static final String MESSAGE = "ERR_GIFTICON_GOODS_EXPOSURE_01::노출되지 않은 상품에는 정렬 순서를 설정할 수 없습니다.";

    public GifticonGoodsNotExposedException() {
        super(MESSAGE);
    }
}
