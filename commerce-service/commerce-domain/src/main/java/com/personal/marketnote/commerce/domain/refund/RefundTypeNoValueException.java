package com.personal.marketnote.commerce.domain.refund;

public class RefundTypeNoValueException extends IllegalArgumentException {
    public RefundTypeNoValueException() {
        super("환불 유형은 필수입니다");
    }
}
