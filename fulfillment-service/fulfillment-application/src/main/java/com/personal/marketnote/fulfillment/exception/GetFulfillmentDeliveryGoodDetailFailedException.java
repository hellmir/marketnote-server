package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class GetFulfillmentDeliveryGoodDetailFailedException extends ExternalOperationFailedException {
    private static final String DEFAULT_MESSAGE = "파스토 출고 상품 상세 목록 조회 중 오류가 발생했습니다.";

    public GetFulfillmentDeliveryGoodDetailFailedException(IOException cause) {
        super(DEFAULT_MESSAGE, cause);
    }

    public GetFulfillmentDeliveryGoodDetailFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasValue(vendorMessage)) {
            return String.format("파스토 출고 상품 상세 목록 조회 실패: %s", vendorMessage);
        }
        return DEFAULT_MESSAGE;
    }
}
