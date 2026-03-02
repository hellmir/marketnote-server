package com.personal.marketnote.commerce.domain.settlement;

public class InvalidSettlementStatusTransitionException extends IllegalStateException {
    public InvalidSettlementStatusTransitionException(SettlementStatus currentStatus) {
        super("현재 상태에서는 전이할 수 없습니다. 현재 상태: " + currentStatus);
    }
}
