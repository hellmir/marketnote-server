package com.personal.marketnote.reward.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class DuplicateUserPointException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "이미 회원 포인트 정보가 생성되어 있습니다.";

    public DuplicateUserPointException() {
        super(MESSAGE);
    }
}
