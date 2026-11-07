package com.personal.marketnote.community.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class ReviewNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "리뷰를 찾을 수 없습니다.";

    public ReviewNotFoundException() {
        super(MESSAGE);
    }
}
