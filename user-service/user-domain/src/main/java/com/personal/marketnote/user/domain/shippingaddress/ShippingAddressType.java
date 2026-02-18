package com.personal.marketnote.user.domain.shippingaddress;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ShippingAddressType {
    HOME("집"),
    COMPANY("회사"),
    OTHER("기타");

    private final String description;
}
