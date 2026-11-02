package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class RegisterFulfillmentShopFailedException extends ExternalOperationFailedException {
    private static final String REGISTER_FULFILLMENT_MARKETNOTE_FAILED_EXCEPTION_MESSAGE =
            "Fulfillment shop registration request failed.";

    public RegisterFulfillmentShopFailedException(IOException cause) {
        super(REGISTER_FULFILLMENT_MARKETNOTE_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public RegisterFulfillmentShopFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return REGISTER_FULFILLMENT_MARKETNOTE_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
