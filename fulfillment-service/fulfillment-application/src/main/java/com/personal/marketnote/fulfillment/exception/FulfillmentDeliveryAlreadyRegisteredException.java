package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class FulfillmentDeliveryAlreadyRegisteredException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "이미 Fulfillment에 출고 요청된 주문입니다.";

    public FulfillmentDeliveryAlreadyRegisteredException() {
        super(MESSAGE);
    }
}
