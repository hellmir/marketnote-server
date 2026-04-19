package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class GetFulfillmentShopsFailedException extends ExternalOperationFailedException {
    private static final String GET_FULFILLMENT_MARKETNOTE_LIST_FAILED_EXCEPTION_MESSAGE =
            "Fulfillment shop list request failed.";

    public GetFulfillmentShopsFailedException(IOException cause) {
        super(GET_FULFILLMENT_MARKETNOTE_LIST_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public GetFulfillmentShopsFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return GET_FULFILLMENT_MARKETNOTE_LIST_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
