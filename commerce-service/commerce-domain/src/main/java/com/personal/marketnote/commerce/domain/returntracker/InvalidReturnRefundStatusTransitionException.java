package com.personal.marketnote.commerce.domain.returntracker;

public class InvalidReturnRefundStatusTransitionException extends IllegalStateException {
    public InvalidReturnRefundStatusTransitionException(ReturnRefundStatus currentStatus, ReturnRefundStatus targetStatus) {
        super("환불 상태 전이 불가: " + currentStatus + " → " + targetStatus);
    }
}
