package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class RegisterFulfillmentSupplierFailedException extends ExternalOperationFailedException {
    private static final String REGISTER_FULFILLMENT_SUPPLIER_FAILED_EXCEPTION_MESSAGE =
            "Fulfillment supplier registration request failed.";

    public RegisterFulfillmentSupplierFailedException(IOException cause) {
        super(REGISTER_FULFILLMENT_SUPPLIER_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public RegisterFulfillmentSupplierFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return REGISTER_FULFILLMENT_SUPPLIER_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
