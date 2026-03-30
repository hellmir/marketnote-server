package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class CompleteFulfillmentDeliveryIcsFailedException extends ExternalOperationFailedException {
    private static final String DEFAULT_MESSAGE = "파스토 배송완료 처리(해외) 중 오류가 발생했습니다.";

    public CompleteFulfillmentDeliveryIcsFailedException(IOException cause) {
        super(DEFAULT_MESSAGE, cause);
    }

    public CompleteFulfillmentDeliveryIcsFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasValue(vendorMessage)) {
            return String.format("파스토 배송완료 처리(해외) 실패: %s", vendorMessage);
        }
        return DEFAULT_MESSAGE;
    }
}
