package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;

import java.io.IOException;

public class RegisterFasstoReturnDeliveryFailedException extends ExternalOperationFailedException {
    private static final String REGISTER_FASSTO_RETURN_DELIVERY_FAILED_EXCEPTION_MESSAGE =
            "Fassto return delivery registration request failed.";

    public RegisterFasstoReturnDeliveryFailedException(IOException cause) {
        super(REGISTER_FASSTO_RETURN_DELIVERY_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public RegisterFasstoReturnDeliveryFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (vendorMessage == null || vendorMessage.isBlank()) {
            return REGISTER_FASSTO_RETURN_DELIVERY_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
