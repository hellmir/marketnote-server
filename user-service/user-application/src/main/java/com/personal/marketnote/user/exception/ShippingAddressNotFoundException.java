package com.personal.marketnote.user.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.Getter;

@Getter
public class ShippingAddressNotFoundException extends DomainNotFoundException {
    private static final String SHIPPING_ADDRESS_NOT_FOUND_EXCEPTION_MESSAGE = "배송지를 찾을 수 없습니다. 전송된 배송지 ID: %d";

    public ShippingAddressNotFoundException(Long shippingAddressId) {
        super(String.format(SHIPPING_ADDRESS_NOT_FOUND_EXCEPTION_MESSAGE, shippingAddressId));
    }
}
