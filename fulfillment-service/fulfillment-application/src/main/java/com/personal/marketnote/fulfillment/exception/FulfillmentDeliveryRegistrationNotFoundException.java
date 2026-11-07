package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class FulfillmentDeliveryRegistrationNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "출고 등록을 찾을 수 없습니다.";

    public FulfillmentDeliveryRegistrationNotFoundException() {
        super(MESSAGE);
    }
}
