package com.personal.marketnote.commerce.domain.settlement;

public class InvalidSettlementStatusTransitionException extends IllegalStateException {
    public InvalidSettlementStatusTransitionException(SettlementStatus currentStatus) {
        super("PENDING 상태에서만 처리할 수 있습니다. 현재 상태: " + currentStatus);
    }
}
