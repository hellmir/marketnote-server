package com.personal.marketnote.reward.domain.exception;

public class GifticonCategoryNotExposedException extends IllegalStateException {
    private static final String MESSAGE = "ERR_GIFTICON_CATEGORY_EXPOSURE_01::노출되지 않은 카테고리에는 노출 순서를 설정할 수 없습니다.";

    public GifticonCategoryNotExposedException() {
        super(MESSAGE);
    }
}
