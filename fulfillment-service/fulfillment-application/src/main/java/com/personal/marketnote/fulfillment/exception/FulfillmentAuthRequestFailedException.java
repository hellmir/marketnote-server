package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class FulfillmentAuthRequestFailedException extends ExternalOperationFailedException {
    private static final String FULFILLMENT_AUTH_REQUEST_FAILED_EXCEPTION_MESSAGE =
            "Fulfillment authentication request failed.";

    public FulfillmentAuthRequestFailedException(IOException cause) {
        super(FULFILLMENT_AUTH_REQUEST_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public FulfillmentAuthRequestFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return FULFILLMENT_AUTH_REQUEST_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
