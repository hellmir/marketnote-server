package com.personal.marketnote.commerce.exception;

import jakarta.persistence.EntityNotFoundException;

public class SettlementPolicyNotFoundException extends EntityNotFoundException {
    public SettlementPolicyNotFoundException(Long id) {
        super("정산 정책을 찾을 수 없습니다. ID: " + id);
    }
}
