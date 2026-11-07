package com.personal.marketnote.fulfillment.domain.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class ShippingTrackerNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "배송 추적 정보를 찾을 수 없습니다.";

    public ShippingTrackerNotFoundException() {
        super(MESSAGE);
    }
}
