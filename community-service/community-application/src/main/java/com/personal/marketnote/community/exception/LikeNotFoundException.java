package com.personal.marketnote.community.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class LikeNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "좋아요를 찾을 수 없습니다.";

    public LikeNotFoundException() {
        super(MESSAGE);
    }
}
