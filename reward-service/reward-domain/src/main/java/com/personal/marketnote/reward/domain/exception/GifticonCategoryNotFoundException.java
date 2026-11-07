package com.personal.marketnote.reward.domain.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class GifticonCategoryNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "기프티콘 카테고리를 찾을 수 없습니다.";

    public GifticonCategoryNotFoundException() {
        super(MESSAGE);
    }
}
