package com.personal.marketnote.commerce.domain.fulfillment;

public class InvalidFulfillmentWorkStatusException extends IllegalArgumentException {
    public InvalidFulfillmentWorkStatusException(String workStatusCode) {
        super("ERR_FULFILLMENT_01::유효하지 않은 풀필먼트 작업 상태입니다. workStatusCode=" + workStatusCode);
    }
}
