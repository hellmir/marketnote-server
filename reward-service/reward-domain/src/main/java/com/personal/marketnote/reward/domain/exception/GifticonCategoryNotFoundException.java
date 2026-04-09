package com.personal.marketnote.reward.domain.exception;

public class GifticonCategoryNotFoundException extends RuntimeException {
    private static final String MESSAGE = "기프티콘 카테고리를 찾을 수 없습니다. id=%d";

    public GifticonCategoryNotFoundException(Long id) {
        super(String.format(MESSAGE, id));
    }
}
