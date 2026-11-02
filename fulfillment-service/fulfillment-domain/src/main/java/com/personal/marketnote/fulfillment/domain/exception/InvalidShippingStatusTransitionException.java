package com.personal.marketnote.fulfillment.domain.exception;

import com.personal.marketnote.fulfillment.domain.shipping.ShippingStatus;

public class InvalidShippingStatusTransitionException extends IllegalStateException {

    public InvalidShippingStatusTransitionException(ShippingStatus currentStatus, ShippingStatus targetStatus) {
        super("배송 상태를 " + currentStatus.getDescription() + "에서 " + targetStatus.getDescription() + "(으)로 변경할 수 없습니다.");
    }

    public InvalidShippingStatusTransitionException(ShippingStatus currentStatus) {
        super("현재 배송 상태(" + currentStatus.getDescription() + ")에서는 해당 작업을 수행할 수 없습니다.");
    }
}
