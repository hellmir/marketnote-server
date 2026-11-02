package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class SettlementPolicyNotFoundException extends DomainNotFoundException {
    public SettlementPolicyNotFoundException(Long id) {
        super("정산 정책을 찾을 수 없습니다. ID: " + id);
    }
}
