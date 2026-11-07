package com.personal.marketnote.community.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class ReviewAlreadyExistsException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "이미 해당 주문에 대한 리뷰가 존재합니다.";

    public ReviewAlreadyExistsException() {
        super(MESSAGE);
    }
}
