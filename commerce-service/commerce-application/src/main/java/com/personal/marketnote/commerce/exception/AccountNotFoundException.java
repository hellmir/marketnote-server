package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class AccountNotFoundException extends DomainNotFoundException {
    public AccountNotFoundException(Long id) {
        super("계정과목을 찾을 수 없습니다. ID: " + id);
    }

    public AccountNotFoundException(String name) {
        super("계정과목을 찾을 수 없습니다. 계정명: " + name);
    }
}
