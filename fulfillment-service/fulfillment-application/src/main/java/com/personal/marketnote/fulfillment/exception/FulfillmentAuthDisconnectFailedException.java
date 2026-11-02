package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class FulfillmentAuthDisconnectFailedException extends ExternalOperationFailedException {
    private static final String FULFILLMENT_AUTH_DISCONNECT_FAILED_EXCEPTION_MESSAGE =
            "Fulfillment authentication disconnect request failed.";

    public FulfillmentAuthDisconnectFailedException(IOException cause) {
        super(FULFILLMENT_AUTH_DISCONNECT_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public FulfillmentAuthDisconnectFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return FULFILLMENT_AUTH_DISCONNECT_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
