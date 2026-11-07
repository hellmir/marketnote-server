package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class ShippingPolicyAlreadyExistsException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "해당 판매자의 배송비 정책이 이미 존재합니다.";

    public ShippingPolicyAlreadyExistsException() {
        super(MESSAGE);
    }
}
