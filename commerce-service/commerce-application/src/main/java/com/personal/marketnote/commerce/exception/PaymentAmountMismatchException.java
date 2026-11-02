package com.personal.marketnote.commerce.exception;

public class PaymentAmountMismatchException extends IllegalStateException {
    public PaymentAmountMismatchException(Long expectedAmount, Long actualAmount) {
        super("주문 금액과 결제 금액이 일치하지 않습니다. 예상: " + expectedAmount + ", 실제: " + actualAmount);
    }
}
