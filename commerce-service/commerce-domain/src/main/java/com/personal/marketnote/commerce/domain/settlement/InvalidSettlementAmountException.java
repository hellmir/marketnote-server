package com.personal.marketnote.commerce.domain.settlement;

public class InvalidSettlementAmountException extends IllegalArgumentException {
    public InvalidSettlementAmountException(String message) {
        super(message);
    }
}
