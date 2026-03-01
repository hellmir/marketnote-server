package com.personal.marketnote.commerce.domain.payment;

public class UnknownPaymentMethodCodeException extends IllegalArgumentException {
    public UnknownPaymentMethodCodeException(String kcpCode) {
        super("알 수 없는 KCP 결제수단 코드: " + kcpCode);
    }
}
