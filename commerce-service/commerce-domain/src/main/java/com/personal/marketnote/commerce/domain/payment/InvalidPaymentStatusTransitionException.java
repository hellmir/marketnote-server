package com.personal.marketnote.commerce.domain.payment;

public class InvalidPaymentStatusTransitionException extends IllegalStateException {
    public InvalidPaymentStatusTransitionException(String expectedStatus, PaymentEventStatus currentStatus) {
        super(expectedStatus + " 현재 상태: " + currentStatus);
    }
}
