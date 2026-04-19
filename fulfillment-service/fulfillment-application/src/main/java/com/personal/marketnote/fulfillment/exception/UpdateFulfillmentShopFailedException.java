package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class UpdateFulfillmentShopFailedException extends ExternalOperationFailedException {
    private static final String UPDATE_FULFILLMENT_MARKETNOTE_FAILED_EXCEPTION_MESSAGE =
            "Fulfillment shop update request failed.";

    public UpdateFulfillmentShopFailedException(IOException cause) {
        super(UPDATE_FULFILLMENT_MARKETNOTE_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public UpdateFulfillmentShopFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return UPDATE_FULFILLMENT_MARKETNOTE_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
