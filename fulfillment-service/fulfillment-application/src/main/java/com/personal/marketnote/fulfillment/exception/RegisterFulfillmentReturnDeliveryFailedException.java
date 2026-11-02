package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class RegisterFulfillmentReturnDeliveryFailedException extends ExternalOperationFailedException {
    private static final String REGISTER_FULFILLMENT_RETURN_DELIVERY_FAILED_EXCEPTION_MESSAGE =
            "Fulfillment return delivery registration request failed.";

    public RegisterFulfillmentReturnDeliveryFailedException(IOException cause) {
        super(REGISTER_FULFILLMENT_RETURN_DELIVERY_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public RegisterFulfillmentReturnDeliveryFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return REGISTER_FULFILLMENT_RETURN_DELIVERY_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
