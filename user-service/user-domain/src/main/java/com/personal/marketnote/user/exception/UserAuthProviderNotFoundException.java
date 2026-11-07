package com.personal.marketnote.user.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class UserAuthProviderNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "존재하지 않는 회원 인증 제공자입니다.";

    public UserAuthProviderNotFoundException() {
        super(MESSAGE);
    }
}
