package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class ShippingTrackerAlreadyExistsException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "이미 배송 추적이 등록된 주문입니다.";

    public ShippingTrackerAlreadyExistsException() {
        super(MESSAGE);
    }
}
