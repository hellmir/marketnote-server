package com.personal.marketnote.fulfillment.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.io.IOException;

public class RegisterFasstoSupplierFailedException extends ExternalOperationFailedException {
    private static final String REGISTER_FASSTO_SUPPLIER_FAILED_EXCEPTION_MESSAGE =
            "Fassto supplier registration request failed.";

    public RegisterFasstoSupplierFailedException(IOException cause) {
        super(REGISTER_FASSTO_SUPPLIER_FAILED_EXCEPTION_MESSAGE, cause);
    }

    public RegisterFasstoSupplierFailedException(String vendorMessage, IOException cause) {
        super(resolveMessage(vendorMessage), cause);
    }

    private static String resolveMessage(String vendorMessage) {
        if (FormatValidator.hasNoValue(vendorMessage)) {
            return REGISTER_FASSTO_SUPPLIER_FAILED_EXCEPTION_MESSAGE;
        }
        return vendorMessage;
    }
}
