package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class CancelFulfillmentDeliveryFailedException extends ExternalOperationFailedException {
    private static final String DEFAULT_MESSAGE = "파스토 출고 취소 요청 중 오류가 발생했습니다.";

    public CancelFulfillmentDeliveryFailedException(IOException cause) {
        super(DEFAULT_MESSAGE, cause);
    }

    public CancelFulfillmentDeliveryFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasValue(vendorMessage)) {
            return String.format("파스토 출고 취소 실패: %s", vendorMessage);
        }
        return DEFAULT_MESSAGE;
    }
}
