package com.personal.marketnote.reward.domain.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class GifticonBrandNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "기프티콘 브랜드를 찾을 수 없습니다.";

    public GifticonBrandNotFoundException() {
        super(MESSAGE);
    }
}
