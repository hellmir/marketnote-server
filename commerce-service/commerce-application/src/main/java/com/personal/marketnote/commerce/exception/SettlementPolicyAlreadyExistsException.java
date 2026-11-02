package com.personal.marketnote.commerce.exception;

public class SettlementPolicyAlreadyExistsException extends IllegalStateException {
    public SettlementPolicyAlreadyExistsException(Long sellerId) {
        super("해당 판매자의 정산 정책이 이미 존재합니다. sellerId=" + sellerId);
    }
}
