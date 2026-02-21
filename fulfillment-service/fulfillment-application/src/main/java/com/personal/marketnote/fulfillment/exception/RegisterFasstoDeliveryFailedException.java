package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class RegisterFasstoDeliveryFailedException extends ExternalOperationFailedException {
    private static final String REGISTER_FASSTO_DELIVERY_FAILED_EXCEPTION_MESSAGE =
            "Fassto delivery registration request failed.";

    public RegisterFasstoDeliveryFailedException(IOException cause) {
        super(REGISTER_FASSTO_DELIVERY_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public RegisterFasstoDeliveryFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return REGISTER_FASSTO_DELIVERY_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
