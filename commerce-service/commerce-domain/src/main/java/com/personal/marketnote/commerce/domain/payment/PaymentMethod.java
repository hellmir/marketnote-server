package com.personal.marketnote.commerce.domain.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CARD("CARD", "신용카드"),
    BANK_TRANSFER("BANK", "계좌이체"),
    VIRTUAL_ACCOUNT("PAVC", "가상계좌"),
    MOBILE("MOBX", "휴대폰결제");

    private final String mobileCode;
    private final String description;

    public static PaymentMethod fromMobileCode(String mobileCode) {
        for (PaymentMethod method : values()) {
            if (method.mobileCode.equals(mobileCode)) {
                return method;
            }
        }
        throw new UnknownPaymentMethodCodeException(mobileCode);
    }
}
