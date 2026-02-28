package com.personal.marketnote.commerce.exception;

import jakarta.persistence.EntityNotFoundException;

public class SettlementNotFoundException extends EntityNotFoundException {
    public SettlementNotFoundException(Long id) {
        super("정산을 찾을 수 없습니다. ID: " + id);
    }
}
