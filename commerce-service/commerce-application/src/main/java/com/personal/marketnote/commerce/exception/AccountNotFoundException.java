package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class AccountNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "계정과목을 찾을 수 없습니다.";

    public AccountNotFoundException() {
        super(MESSAGE);
    }
}
