package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentWorkStatus;

public class FulfillmentDeliveryCancellationNotAllowedException extends RuntimeException {
    public FulfillmentDeliveryCancellationNotAllowedException(FulfillmentWorkStatus currentStatus) {
        super("현재 작업 상태에서는 출고 취소가 불가합니다. workStatus=" + currentStatus.name());
    }
}
