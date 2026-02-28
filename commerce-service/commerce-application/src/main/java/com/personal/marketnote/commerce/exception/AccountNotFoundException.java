package com.personal.marketnote.commerce.exception;

import jakarta.persistence.EntityNotFoundException;

public class AccountNotFoundException extends EntityNotFoundException {
    public AccountNotFoundException(Long id) {
        super("계정과목을 찾을 수 없습니다. ID: " + id);
    }

    public AccountNotFoundException(String name) {
        super("계정과목을 찾을 수 없습니다. 계정명: " + name);
    }
}
