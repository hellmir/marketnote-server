package com.personal.marketnote.reward.domain.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class GifticonOrderNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "기프티콘 주문을 찾을 수 없습니다.";

    public GifticonOrderNotFoundException() {
        super(MESSAGE);
    }
}
