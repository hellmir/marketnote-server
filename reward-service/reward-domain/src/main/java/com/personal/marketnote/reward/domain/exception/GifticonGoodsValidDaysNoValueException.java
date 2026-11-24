package com.personal.marketnote.reward.domain.exception;

public class GifticonGoodsValidDaysNoValueException extends RuntimeException {

    public GifticonGoodsValidDaysNoValueException(String goodsCode) {
        super("ERR_GIFTICON_GOODS_VALID_DAYS::기프티콘 상품에 유효기간(validDays)이 설정되어 있지 않습니다. goodsCode=" + goodsCode);
    }
}
