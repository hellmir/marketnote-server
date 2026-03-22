package com.personal.marketnote.commerce.domain.refund;

public class RefundOrderIdNoValueException extends IllegalArgumentException {
    public RefundOrderIdNoValueException() {
        super("주문 ID는 필수입니다");
    }
}
