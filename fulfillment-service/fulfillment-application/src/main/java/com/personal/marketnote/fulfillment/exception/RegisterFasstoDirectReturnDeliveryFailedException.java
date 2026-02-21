package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class RegisterFasstoDirectReturnDeliveryFailedException extends ExternalOperationFailedException {
    private static final String REGISTER_FASSTO_DIRECT_RETURN_DELIVERY_FAILED_EXCEPTION_MESSAGE =
            "Fassto direct return delivery registration request failed.";

    public RegisterFasstoDirectReturnDeliveryFailedException(IOException cause) {
        super(REGISTER_FASSTO_DIRECT_RETURN_DELIVERY_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public RegisterFasstoDirectReturnDeliveryFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return REGISTER_FASSTO_DIRECT_RETURN_DELIVERY_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
