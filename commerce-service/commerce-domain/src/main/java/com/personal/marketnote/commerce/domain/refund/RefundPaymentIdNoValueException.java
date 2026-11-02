package com.personal.marketnote.commerce.domain.refund;

public class RefundPaymentIdNoValueException extends IllegalArgumentException {
    public RefundPaymentIdNoValueException() {
        super("결제 ID는 필수입니다");
    }
}
