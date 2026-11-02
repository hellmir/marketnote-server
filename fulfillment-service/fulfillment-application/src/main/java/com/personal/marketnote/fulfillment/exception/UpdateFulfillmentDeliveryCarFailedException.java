package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class UpdateFulfillmentDeliveryCarFailedException extends ExternalOperationFailedException {
    private static final String DEFAULT_MESSAGE = "풀필먼트 출고 수정(차량) 중 오류가 발생했습니다.";

    public UpdateFulfillmentDeliveryCarFailedException(IOException cause) {
        super(DEFAULT_MESSAGE, cause);
    }

    public UpdateFulfillmentDeliveryCarFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasValue(vendorMessage)) {
            return String.format("풀필먼트 출고 수정(차량) 실패: %s", vendorMessage);
        }
        return DEFAULT_MESSAGE;
    }
}
