package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class RegisterFulfillmentDirectReturnDeliveryFailedException extends ExternalOperationFailedException {
    private static final String REGISTER_FULFILLMENT_DIRECT_RETURN_DELIVERY_FAILED_EXCEPTION_MESSAGE =
            "Fulfillment direct return delivery registration request failed.";

    public RegisterFulfillmentDirectReturnDeliveryFailedException(IOException cause) {
        super(REGISTER_FULFILLMENT_DIRECT_RETURN_DELIVERY_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public RegisterFulfillmentDirectReturnDeliveryFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return REGISTER_FULFILLMENT_DIRECT_RETURN_DELIVERY_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
