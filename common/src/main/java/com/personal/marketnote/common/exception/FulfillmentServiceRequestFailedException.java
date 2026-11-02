package com.personal.marketnote.common.exception;

import java.io.IOException;

public class FulfillmentServiceRequestFailedException extends ExternalOperationFailedException {
    private static final String FULFILLMENT_SERVICE_REQUEST_FAILED_EXCEPTION_MESSAGE =
            "풀필먼트 서비스 통신 중 오류가 발생했습니다.";

    public FulfillmentServiceRequestFailedException(IOException cause) {
        super(String.format(FULFILLMENT_SERVICE_REQUEST_FAILED_EXCEPTION_MESSAGE), cause);
    }
}
