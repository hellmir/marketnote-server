package com.personal.marketnote.commerce.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class ShippingAddressNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "배송지를 찾을 수 없습니다.";

    public ShippingAddressNotFoundException() {
        super(MESSAGE);
    }
}
