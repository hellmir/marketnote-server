package com.personal.marketnote.commerce.domain.settlement;

public class InvalidSettlementPolicyException extends IllegalArgumentException {
    public InvalidSettlementPolicyException(String message) {
        super(message);
    }
}
