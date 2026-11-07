package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class ReturnTrackerNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "반품 추적 정보를 찾을 수 없습니다.";

    public ReturnTrackerNotFoundException() {
        super(MESSAGE);
    }
}
