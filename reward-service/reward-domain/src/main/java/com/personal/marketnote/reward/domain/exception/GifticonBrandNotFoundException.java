package com.personal.marketnote.reward.domain.exception;

public class GifticonBrandNotFoundException extends RuntimeException {
    private static final String MESSAGE = "기프티콘 브랜드를 찾을 수 없습니다. brandCode=%s";

    public GifticonBrandNotFoundException(String brandCode) {
        super(String.format(MESSAGE, brandCode));
    }
}
