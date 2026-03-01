package com.personal.marketnote.commerce.domain.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CARD("PACA", "CARD", "신용카드"),
    BANK_TRANSFER("PABK", "BANK", "계좌이체"),
    VIRTUAL_ACCOUNT("PAVC", "PAVC", "가상계좌"),
    MOBILE("PAMC", "MOBX", "휴대폰결제");

    private final String kcpCode;
    private final String mobileCode;
    private final String description;

    public static PaymentMethod fromKcpCode(String kcpCode) {
        for (PaymentMethod method : values()) {
            if (method.kcpCode.equals(kcpCode)) {
                return method;
            }
        }
        throw new UnknownPaymentMethodCodeException(kcpCode);
    }
}
