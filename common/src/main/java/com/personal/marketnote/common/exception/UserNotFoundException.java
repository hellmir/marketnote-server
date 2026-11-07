package com.personal.marketnote.common.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class UserNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "회원을 찾을 수 없습니다.";

    public UserNotFoundException() {
        super(MESSAGE);
    }
}
